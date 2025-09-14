package com.myobf.transformer;

import com.myobf.util.AESUtil;
import com.myobf.util.ASMUtils;
import com.myobf.util.Dictionary;
import com.myobf.util.Local;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StringEncryptTransformer implements Opcodes {

    private final Random random = new SecureRandom();

    public void transform(List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes) {
            if (Modifier.isInterface(classNode.access)) continue;

            String fieldName = Dictionary.FIELD.getNewName(classNode);
            List<String> pool = new ArrayList<>();

            for (MethodNode method : classNode.methods) {
                for (AbstractInsnNode ain : method.instructions.toArray()) {
                    if (!(ain instanceof LdcInsnNode ldc)) continue;
                    if (!(ldc.cst instanceof String str)) continue;

                    int idx = pool.size();
                    InsnList list = new InsnList();

                    list.add(new FieldInsnNode(GETSTATIC, classNode.name, fieldName, "[Ljava/lang/String;"));
                    list.add(ASMUtils.pushInt(idx));
                    list.add(new InsnNode(AALOAD));

                    method.instructions.insertBefore(ldc, list);
                    method.instructions.remove(ldc);

                    pool.add(str);
                }
            }

            if (!pool.isEmpty()) {
                generateClinit(classNode, fieldName, pool);
                classNode.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, fieldName, "[Ljava/lang/String;", null, null));
            }
        }
    }

    private String xor(String str, int key, int key2) {
        char[] arr = str.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            arr[i] ^= key ^ key2;
        }
        return new String(arr);
    }

    @SuppressWarnings("all")
    private void generateClinit(ClassNode classNode, String fieldName, List<String> pool) {
        MethodNode method = classNode.methods.stream()
                .filter(m -> m.name.equals("<clinit>"))
                .findAny()
                .orElseGet(() -> {
                    MethodNode mn = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
                    classNode.methods.add(mn);
                    mn.instructions.add(new InsnNode(RETURN));
                    return mn;
                });

        // Generate random encryption choice
        boolean useAES = random.nextBoolean();

        // Generate keys and IVs
        byte[] key = new byte[16];
        byte[] iv = new byte[16];
        random.nextBytes(key);
        random.nextBytes(iv);

        int key1 = random.nextInt();
        int key2 = random.nextInt();

        // Combine all strings into one and record their lengths
        StringBuilder sb = new StringBuilder();
        char[] lenArr = new char[pool.size()];
        for (int i = 0; i < pool.size(); i++) {
            String str = pool.get(i);
            sb.append(str);
            lenArr[i] = (char) str.length();
        }

        String combined = sb.toString();
        String xorred = xor(combined, key1, key2);
        String encrypted = useAES
                ? AESUtil.encrypt(xorred, key, iv)
                : AESUtil.encryptChaCha20(xorred, key, iv); // Support ChaCha20 optionally

        // Locals setup
        Local alloc = Local.allocObject(method);
        Local lenStrVar = Local.allocObject(method);
        Local keyVar = Local.allocObject(method);
        Local ivVar = Local.allocObject(method);
        Local ciphVar = Local.allocObject(method);
        Local charArr = Local.allocObject(method);
        Local strArr = Local.allocObject(method);
        Local iVar = Local.alloc(method, Type.INT_TYPE);
        Local ptrVar = Local.alloc(method, Type.INT_TYPE);
        Local lenVar = Local.alloc(method, Type.INT_TYPE);

        InsnList list = new InsnList();

        // Insert dummy instructions to confuse analysis
        list.add(new InsnNode(NOP));
        list.add(new InsnNode(NOP));

        // Encryption setup
        list.add(new LdcInsnNode(encrypted));
        list.add(alloc.store());
        list.add(new LdcInsnNode(new String(lenArr)));
        list.add(lenStrVar.store());
        list.add(new LdcInsnNode(new String(key, StandardCharsets.ISO_8859_1)));
        list.add(new LdcInsnNode("ISO-8859-1"));
        list.add(new InsnNode(DUP_X1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/lang/String;)[B"));
        list.add(keyVar.store());
        list.add(new LdcInsnNode(new String(iv, StandardCharsets.ISO_8859_1)));
        list.add(new InsnNode(SWAP));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/lang/String;)[B"));
        list.add(ivVar.store());

        if (useAES) {
            // AES encryption setup
            list.add(new LdcInsnNode("AES/CBC/PKCS5Padding"));
            list.add(new MethodInsnNode(INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;"));
            list.add(ciphVar.store());
            list.add(ciphVar.load());
            list.add(new InsnNode(ICONST_2)); // Cipher.DECRYPT_MODE
            list.add(new TypeInsnNode(NEW, "javax/crypto/spec/SecretKeySpec"));
            list.add(new InsnNode(DUP));
            list.add(keyVar.load());
            list.add(new LdcInsnNode("AES"));
            list.add(new MethodInsnNode(INVOKESPECIAL, "javax/crypto/spec/SecretKeySpec", "<init>", "([BLjava/lang/String;)V"));
            list.add(new TypeInsnNode(NEW, "javax/crypto/spec/IvParameterSpec"));
            list.add(new InsnNode(DUP));
            list.add(ivVar.load());
            list.add(new MethodInsnNode(INVOKESPECIAL, "javax/crypto/spec/IvParameterSpec", "<init>", "([B)V"));
            list.add(new MethodInsnNode(INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V"));
        } else {
            // ChaCha20 encryption setup (pseudo-code placeholder)
            list.add(new LdcInsnNode("ChaCha20"));
            list.add(new MethodInsnNode(INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;"));
            list.add(ciphVar.store());
            list.add(ciphVar.load());
            list.add(new InsnNode(ICONST_2)); // Cipher.DECRYPT_MODE
            list.add(new TypeInsnNode(NEW, "javax/crypto/spec/SecretKeySpec"));
            list.add(new InsnNode(DUP));
            list.add(keyVar.load());
            list.add(new LdcInsnNode("ChaCha20"));
            list.add(new MethodInsnNode(INVOKESPECIAL, "javax/crypto/spec/SecretKeySpec", "<init>", "([BLjava/lang/String;)V"));
            list.add(new TypeInsnNode(NEW, "javax/crypto/spec/IvParameterSpec"));
            list.add(new InsnNode(DUP));
            list.add(ivVar.load());
            list.add(new MethodInsnNode(INVOKESPECIAL, "javax/crypto/spec/IvParameterSpec", "<init>", "([B)V"));
            list.add(new MethodInsnNode(INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V"));
        }

        // Decryption logic
        list.add(new TypeInsnNode(NEW, "java/lang/String"));
        list.add(new InsnNode(DUP));
        list.add(ciphVar.load());
        list.add(new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"));
        list.add(alloc.load());
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B"));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"));
        list.add(alloc.store());
        list.add(alloc.load());
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C"));
        list.add(charArr.store());

        // Split into strings
        list.add(ASMUtils.pushInt(pool.size()));
        list.add(new TypeInsnNode(ANEWARRAY, "java/lang/String"));
        list.add(strArr.store());

        LabelNode loopStart = new LabelNode();
        LabelNode loopEnd = new LabelNode();

        list.add(new InsnNode(ICONST_0));
        list.add(iVar.store());
        list.add(loopStart);
        list.add(iVar.load());
        list.add(charArr.load());
        list.add(new InsnNode(ARRAYLENGTH));
        list.add(new JumpInsnNode(IF_ICMPGE, loopEnd));

        // XOR decryption step
        list.add(charArr.load());
        list.add(iVar.load());
        list.add(new InsnNode(DUP2));
        list.add(new InsnNode(CALOAD));
        list.add(ASMUtils.pushInt(key1));
        list.add(new InsnNode(IXOR));
        list.add(ASMUtils.pushInt(key2));
