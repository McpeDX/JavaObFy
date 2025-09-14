package com.myobf.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Random;

public class DeadCodeInjector implements Transformer, Opcodes {

    private final Random random = new Random();

    @Override
    public void transform(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            // Skip abstract methods, constructors, or native methods
            if ((mn.access & ACC_ABSTRACT) != 0 ||
                (mn.access & ACC_NATIVE) != 0 ||
                mn.name.equals("<init>") ||
                mn.name.equals("<clinit>")) {
                continue;
            }

            // Inject at method entry
            mn.instructions.insertBefore(mn.instructions.getFirst(), createRandomDeadCodeBlock());

            // Inject before each return statement
            for (AbstractInsnNode insn : mn.instructions.toArray()) {
                if (isReturn(insn.getOpcode())) {
                    mn.instructions.insertBefore(insn, createRandomDeadCodeBlock());
                }
            }
        }
    }

    private boolean isReturn(int opcode) {
        return opcode >= IRETURN && opcode <= RETURN;
    }

    private InsnList createRandomDeadCodeBlock() {
        switch (random.nextInt(3)) {
            case 0:
                return createDeadCodeBlockAlwaysFalse();
            case 1:
                return createDeadCodeBlockLoop();
            default:
                return createDeadCodeBlockException();
        }
    }

    // Dead code with an always-false condition
    private InsnList createDeadCodeBlockAlwaysFalse() {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();

        list.add(new InsnNode(ICONST_0)); // false
        list.add(new JumpInsnNode(IFEQ, label)); // jump if false

        list.add(new LdcInsnNode("Dead code block"));
        list.add(new InsnNode(POP));

        list.add(label);
        return list;
    }

    // Dead code with an infinite-looking but harmless loop
    private InsnList createDeadCodeBlockLoop() {
        InsnList list = new InsnList();
        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();

        list.add(start);
        list.add(new InsnNode(ICONST_0));
        list.add(new InsnNode(POP)); // dummy operation
        list.add(new JumpInsnNode(GOTO, end)); // jump to end

        list.add(new LdcInsnNode("This loop does nothing"));
        list.add(new InsnNode(POP));

        list.add(end);
        return list;
    }

    // Dead code that throws an exception but never gets executed
    private InsnList createDeadCodeBlockException() {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();

        list.add(new InsnNode(ICONST_0)); // false
        list.add(new JumpInsnNode(IFNE, label)); // jump if true

        // Throw an exception
        list.add(new TypeInsnNode(NEW, "java/lang/IllegalStateException"));
        list.add(new InsnNode(DUP));
        list.add(new LdcInsnNode(encryptString("This should never happen")));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V", false));
        list.add(new InsnNode(ATHROW));

        list.add(label);
        return list;
    }

    // Simple XOR encryption for demonstration
    private String encryptString(String input) {
        char key = (char) (random.nextInt(26) + 'A');
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append((char) (c ^ key));
        }
        // For simplicity, just append the key as the last character
        sb.append(key);
        return sb.toString();
    }
}
