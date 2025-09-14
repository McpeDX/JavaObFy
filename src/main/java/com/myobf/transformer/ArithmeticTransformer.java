package com.myobf.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArithmeticTransformer implements Transformer, Opcodes {

    private final Random random = new Random();

    @Override
    public void transform(ClassNode cn) {
        for (var method : cn.methods) {
            if ((method.access & ACC_NATIVE) != 0) {
                continue; // Skip native methods
            }

            InsnList instructions = method.instructions;
            if (instructions == null || instructions.size() == 0) {
                continue;
            }

            for (var insn : instructions.toArray()) {
                InsnList replacement = new InsnList();

                switch (insn.getOpcode()) {
                    case IADD -> applyIAdd(replacement);
                    case ISUB -> applyISub(replacement);
                    case IXOR -> applyIXor(replacement);
                    case IAND -> applyIAnd(replacement);
                    case IOR  -> applyIOr(replacement);
                    case IMUL -> applyIMul(replacement);
                    // Extendable: Add more cases like LMUL, etc.
                }

                if (replacement.size() > 0) {
                    addDeadCode(replacement);
                    instructions.insertBefore(insn, replacement);
                    instructions.remove(insn);
                }
            }
        }
    }

    private void applyIAdd(InsnList list) {
        List<InsnList> patterns = new ArrayList<>();

        // Pattern 1: Use bitwise manipulation
        InsnList pattern1 = new InsnList();
        pattern1.add(new InsnNode(DUP2));
        pattern1.add(new InsnNode(IAND));
        pattern1.add(new InsnNode(DUP_X2));
        pattern1.add(new InsnNode(POP));
        pattern1.add(new InsnNode(IOR));
        pattern1.add(new InsnNode(IADD));

        // Pattern 2: Add 1 and subtract 1
        InsnList pattern2 = new InsnList();
        pattern2.add(new InsnNode(ICONST_1));
        pattern2.add(new InsnNode(IADD));
        pattern2.add(new InsnNode(ICONST_1));
        pattern2.add(new InsnNode(ISUB));

        patterns.add(pattern1);
        patterns.add(pattern2);

        InsnList selected = patterns.get(random.nextInt(patterns.size()));
        list.add(selected);
    }

    private void applyISub(InsnList list) {
        InsnList pattern = new InsnList();
        pattern.add(new InsnNode(INEG));
        pattern.add(new InsnNode(DUP2));
        pattern.add(new InsnNode(IXOR));
        pattern.add(new InsnNode(DUP_X2));
        pattern.add(new InsnNode(POP));
        pattern.add(new InsnNode(IAND));
        pattern.add(new InsnNode(ICONST_2));
        pattern.add(new InsnNode(IMUL));
        pattern.add(new InsnNode(IADD));
        list.add(pattern);
    }

    private void applyIXor(InsnList list) {
        InsnList pattern = new InsnList();
        pattern.add(new InsnNode(DUP2));
        pattern.add(new InsnNode(IOR));
        pattern.add(new InsnNode(DUP_X2));
        pattern.add(new InsnNode(POP));
        pattern.add(new InsnNode(IAND));
        pattern.add(new InsnNode(ISUB));
        list.add(pattern);
    }

    private void applyIAnd(InsnList list) {
        InsnList pattern = new InsnList();
        pattern.add(new InsnNode(DUP2));
        pattern.add(new InsnNode(IADD));
        pattern.add(new InsnNode(DUP_X2));
        pattern.add(new InsnNode(POP));
        pattern.add(new InsnNode(IOR));
        pattern.add(new InsnNode(ISUB));
        list.add(pattern);
    }

    private void applyIOr(InsnList list) {
        InsnList pattern = new InsnList();
        pattern.add(new InsnNode(DUP2));
        pattern.add(new InsnNode(ICONST_M1));
        pattern.add(new InsnNode(IXOR));
        pattern.add(new InsnNode(SWAP));
        pattern.add(new InsnNode(ICONST_M1));
        pattern.add(new InsnNode(IXOR));
        pattern.add(new InsnNode(SWAP));
        pattern.add(new InsnNode(DUP2_X2));
        pattern.add(new InsnNode(POP2));
        pattern.add(new InsnNode(IADD));
        pattern.add(new InsnNode(ICONST_1));
        pattern.add(new InsnNode(IADD));
        pattern.add(new InsnNode(DUP_X2));
        pattern.add(new InsnNode(POP));
        pattern.add(new InsnNode(IOR));
        pattern.add(new InsnNode(IADD));
        list.add(pattern);
    }

    private void applyIMul(InsnList list) {
        // Example: Encode constants before multiplication
        int constant = 5;
        int key = random.nextInt(255);
        int encrypted = constant ^ key;

        list.add(new LdcInsnNode(encrypted));
        list.add(new LdcInsnNode(key));
        list.add(new InsnNode(IXOR)); // decrypt
        list.add(new InsnNode(IMUL));
    }

    private void addDeadCode(InsnList list) {
        if (random.nextBoolean()) {
            // Add NOP
            list.add(new InsnNode(NOP));
        }
        if (random.nextBoolean()) {
            // Add dummy constant push and pop
            list.add(new InsnNode(ICONST_0));
            list.add(new InsnNode(POP));
        }
        if (random.nextBoolean()) {
            // Add unreachable code block
            list.add(new InsnNode(ICONST_1));
            list.add(new InsnNode(POP));
            list.add(new InsnNode(NOP));
        }
    }
}
