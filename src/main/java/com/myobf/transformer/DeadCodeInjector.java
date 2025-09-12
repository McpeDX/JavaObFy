package com.myobf.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class DeadCodeInjector implements Transformer, Opcodes {

    @Override
    public void transform(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            // Skip abstract methods and constructors
            if ((mn.access & ACC_ABSTRACT) != 0 || mn.name.equals("<init>") || mn.name.equals("<clinit>")) {
                continue;
            }

            // Find all return statements
            for (AbstractInsnNode insn : mn.instructions) {
                if (isReturn(insn.getOpcode())) {
                    mn.instructions.insertBefore(insn, createDeadCodeBlock());
                }
            }
        }
    }

    private boolean isReturn(int opcode) {
        return opcode >= IRETURN && opcode <= RETURN;
    }

    private InsnList createDeadCodeBlock() {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();

        // An if statement that is always false
        list.add(new InsnNode(ICONST_0)); // push false
        list.add(new JumpInsnNode(IFEQ, label)); // jump if false

        // A block of junk code that will never be executed
        list.add(new LdcInsnNode("This is dead code"));
        list.add(new InsnNode(POP));
        list.add(new TypeInsnNode(NEW, "java/lang/RuntimeException"));
        list.add(new InsnNode(DUP));
        list.add(new LdcInsnNode("You should not see this"));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V"));
        list.add(new InsnNode(ATHROW));

        // The label that is the target of the jump
        list.add(label);

        return list;
    }
}
