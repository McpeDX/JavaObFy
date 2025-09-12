package com.myobf.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
// import org.objectweb.asm.tree.analysis.Frame; // Temporarily removed

import java.util.Map;
import java.util.stream.IntStream;

public class ASMUtils implements Opcodes {

    public static /* Map<AbstractInsnNode, Frame> */ Object analyzeMethod(ClassNode owner, MethodNode method) {
        // TODO: Implement this using me.coley.analysis dependency
        System.err.println("Warning: Frame analysis is not implemented. FlowFlattenTransformer will not work yet.");
        return null;
    }

    public static int getInt(AbstractInsnNode insn) {
        if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof Integer num) {
            return num;
        }
        if (insn instanceof IntInsnNode node && node.getOpcode() != NEWARRAY) {
            return node.operand;
        }
        if (insn.getOpcode() >= ICONST_M1 && insn.getOpcode() <= ICONST_5) {
            return insn.getOpcode() - ICONST_0;
        }
        throw new IllegalArgumentException("Instruction is not a valid integer push.");
    }

    public static AbstractInsnNode pushInt(int num) {
        if (num >= -1 && num <= 5) {
            return new InsnNode(ICONST_0 + num);
        }
        if (num >= Byte.MIN_VALUE && num <= Byte.MAX_VALUE) {
            return new IntInsnNode(BIPUSH, num);
        }
        if (num >= Short.MIN_VALUE && num <= Short.MAX_VALUE) {
            return new IntInsnNode(SIPUSH, num);
        }
        return new LdcInsnNode(num);
    }

    public static void sortSwitch(LookupSwitchInsnNode sw) {
        var entries = IntStream.range(0, sw.keys.size())
                .mapToObj(i -> Map.entry(sw.keys.get(i), sw.labels.get(i)))
                .sorted(Map.Entry.comparingByKey())
                .toList();

        sw.keys.clear();
        sw.labels.clear();

        for (var entry : entries) {
            sw.keys.add(entry.getKey());
            sw.labels.add(entry.getValue());
        }
    }

    public static void translateConcatenation(MethodNode methodNode) {
        // TODO: Implement this robustly. This is a placeholder.
    }

    public static boolean isValidIntPush(AbstractInsnNode insn) {
        if (insn == null) return false;
        int opcode = insn.getOpcode();
        if (opcode >= ICONST_M1 && opcode <= ICONST_5) return true;
        if (opcode == BIPUSH || opcode == SIPUSH) return true;
        if (insn instanceof LdcInsnNode ldc) {
            return ldc.cst instanceof Integer;
        }
        return false;
    }
}
