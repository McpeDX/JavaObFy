package com.myobf.transformer;

import com.myobf.util.AESUtil;
import com.myobf.util.ASMUtils;
import com.myobf.util.Dictionary;
import com.myobf.util.Local;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntegerEncryptTransformer implements Opcodes {

    private final Random random = new SecureRandom();

    public void transform(List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes) {
            if (Modifier.isInterface(classNode.access))
                continue;

            String fieldName = Dictionary.FIELD.getNewName(classNode);
            List<Integer> pool = new ArrayList<>();
            byte[] key = generateKey();
            byte[] iv = generateIv();

            for (MethodNode method : classNode.methods) {
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (!ASMUtils.isValidIntPush(insn))
                        continue;

                    InsnList replacement = new InsnList();
                    replacement.add(new FieldInsnNode(GETSTATIC, classNode.name, fieldName, "[I"));
                    replacement.add(ASMUtils.pushInt(pool.size()));
                    replacement.add(new InsnNode(IALOAD));

                    int number = ASMUtils.getInt(insn);
                    pool.add(number);

                    method.instructions.insertBefore(insn, replacement);
                    method.instructions.remove(insn);
                }
            }

            if (!pool.isEmpty()) {
                generateClinit(classNode, fieldName, pool, key, iv);
                classNode.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, fieldName, "[I", null, null));
            }
        }
    }

    private byte[] generateKey() {
        byte[] key = new byte[16];
        random.nextBytes(key);
        return key;
    }

    private byte[] generateIv() {
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
    }

    private byte[] intToBytes(int i) {
        return new byte[] {
            (byte) (i >> 24),
            (byte) (i >> 16),
            (byte) (i >> 8),
            (byte) i
        };
    }

    private void generateClinit(ClassNode classNode, String fieldName, List<Integer> pool, byte[] key, byte[] iv) {
        MethodNode method = classNode.methods.stream()
                .filter(m -> m.name.equals("<clinit>"))
                .findFirst()
                .orElseGet(() -> {
                    MethodNode mn = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
                    classNode.methods.add(mn);
                    mn.instructions.add(new InsnNode(RETURN));
                    return mn;
                });

        int xorKey = random.nextInt();
        StringBuilder sb = new StringBuilder();
        for (int num : pool) {
            sb.append(new String(intToBytes(num ^ xorKey), StandardCharsets.ISO_8859_1));
        }

        byte[] encrypted = AESUtil.encrypt(sb.toString(), key, iv);

        InsnList list = new InsnList();
        LabelNode startLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();

        // Dummy code start
        list.add(new InsnNode(NOP));
        list.add(startLabel);

        // Allocate objects and set up encryption
        Local alloc = Local.allocObject(method);
        Local keyBytes = Local.allocObject(method);
        Local ivBytes = Local.allocObject(method);
        Local cipher = Local.allocObject(method);
        Local i = Local.alloc(method, Type.INT_TYPE);
        Local ptr = Local.alloc(method, Type.INT_TYPE);
        Local arr = Local.allocObject(method);
        Local bytes = Local.allocObject(method);

        // Load encrypted data
        list.add(new LdcInsnNode(encrypted));
        list.add(alloc.store());

        list.add(new LdcInsnNode(new String(key, StandardCharsets.ISO_8859_1)));
        list.add(new LdcInsnNode("ISO-8859-1"));
        list.add(new InsnNode(DUP_X1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/lang/String;)[B"));
        list.add(keyBytes.store());

        list.add(new LdcInsnNode(new String(iv, StandardCharsets.ISO_8859_1)));
        list.add(new InsnNode(SWAP));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/lang/String;)[B"));
        list.add(ivBytes.store());

        list.add(new LdcInsnNode("AES/CBC/PKCS5Padding"));
        list.add(new MethodInsnNode(INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;"));
        list.add(cipher.store());

        list.add(cipher.load());
        list.add(new InsnNode(ICONST_2)); // Cipher.DECRYPT_MODE
        list.add(new TypeInsnNode(NEW, "javax/crypto/spec/SecretKeySpec"));
        list.add(new InsnNode(DUP));
        list.add(keyBytes.load());
        list.add(new LdcInsnNode("AES"));
        list.add(new MethodInsnNode(INVOKESPECIAL, "javax/crypto/spec/SecretKeySpec", "<init>", "([BLjava/lang/String;)V"));
        list.add(new TypeInsnNode(NEW, "javax/crypto/spec/IvParameterSpec"));
        list.add(new InsnNode(DUP));
        list.add(ivBytes.load());
        list.add(new MethodInsnNode(INVOKESPECIAL, "javax/crypto/spec/IvParameterSpec", "<init>", "([B)V"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V"));

        // Decrypt and process the data
        list.add(new TypeInsnNode(NEW, "java/lang/String"));
        list.add(new InsnNode(DUP));
        list.add(cipher.load());
        list.add(new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"));
        list.add(alloc.load());
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B"));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"));
        list.add(alloc.store());

        // Convert string back to integers
        list.add(new InsnNode(ICONST_0));
        list.add(new InsnNode(DUP));
        list.add(i.store());
        list.add(ptr.store());
        list.add(alloc.load());
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "length", "()I"));
        list.add(new InsnNode(ICONST_4));
        list.add(new InsnNode(IDIV));
        list.add(new IntInsnNode(NEWARRAY, T_INT));
        list.add(arr.store());

        LabelNode loopStart = new LabelNode();
        list.add(loopStart);
        list.add(alloc.load());
        list.add(ptr.load());
        list.add(new InsnNode(DUP));
        list.add(new InsnNode(ICONST_4));
        list.add(new InsnNode(IADD));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"));
        list.add(new LdcInsnNode("ISO-8859-1"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/lang/String;)[B"));
        list.add(bytes.store());

        // Convert bytes to int and XOR with key
        list.add(bytes.load());
        list.add(new InsnNode(ICONST_0));
        list.add(new InsnNode(BALOAD));
        list.add(new IntInsnNode(SIPUSH, 255));
        list.add(new InsnNode(IAND));
        list.add(new IntInsnNode(SIPUSH, 24));
        list.add(new InsnNode(ISHL));

        list.add(bytes.load());
        list.add(new InsnNode(ICONST_1));
        list.add(new InsnNode(BALOAD));
        list.add(new IntInsnNode(SIPUSH, 255));
        list.add(new InsnNode(IAND));
        list.add(new IntInsnNode(SIPUSH, 16));
        list.add(new InsnNode(ISHL));
        list.add(new InsnNode(IOR));

        list.add(bytes.load());
        list.add(new InsnNode(ICONST_2));
        list.add(new InsnNode(BALOAD));
        list.add(new IntInsnNode(SIPUSH, 255));
        list.add(new InsnNode(IAND));
        list.add(new IntInsnNode(SIPUSH, 8));
        list.add(new InsnNode(ISHL));
        list.add(new InsnNode(IOR));

        list.add(bytes.load());
        list.add(new InsnNode(ICONST_3));
        list.add(new InsnNode(BALOAD));
        list.add(new IntInsnNode(SIPUSH, 255));
        list.add(new InsnNode(IAND));
        list.add(new InsnNode(IOR));

        list.add(new LdcInsnNode(xorKey));
        list.add(new InsnNode(IXOR));

        list.add(arr.load());
        list.add(i.load());
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP2));
        list.add(new InsnNode(IASTORE));

        list.add(new IincInsnNode(i.getIndex(), 1));
        list.add(new IincInsnNode(ptr.getIndex(), 4));

        list.add(i.load());
        list.add(arr.load());
        list.add(new InsnNode(ARRAYLENGTH));
        list.add(new JumpInsnNode(IF_ICMPLT, loopStart));

        list.add(arr.load());
        list.add(new FieldInsnNode(PUTSTATIC, classNode.name, fieldName, "[I"));

        // Dummy code end
        list.add(endLabel);
        list.add(new InsnNode(NOP));

        method.instructions.insert(list);
    }
}
