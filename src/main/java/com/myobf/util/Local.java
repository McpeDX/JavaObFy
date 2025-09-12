package com.myobf.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class Local {

    private final int index;
    private final Type type;

    private Local(int index, Type type) {
        this.index = index;
        this.type = type;
    }

    /**
     * Creates a new local variable in the given method.
     * It allocates a new index and updates the method's maxLocals.
     */
    public static Local alloc(MethodNode method, Type type) {
        int newIndex = method.maxLocals;
        method.maxLocals += type.getSize();
        return new Local(newIndex, type);
    }

    /**
     * Creates a new local variable for an object type.
     */
    public static Local allocObject(MethodNode method) {
        return alloc(method, Type.getType(Object.class));
    }

    /**
     * Returns an instruction to load this local variable.
     */
    public VarInsnNode load() {
        return new VarInsnNode(type.getOpcode(org.objectweb.asm.Opcodes.ILOAD), index);
    }

    /**
     * Returns an instruction to store this local variable.
     */
    public VarInsnNode store() {
        return new VarInsnNode(type.getOpcode(org.objectweb.asm.Opcodes.ISTORE), index);
    }

    public int getIndex() {
        return index;
    }
}
