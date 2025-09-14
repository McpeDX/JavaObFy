package com.myobf.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class AnnotationStripper implements Transformer {

    @Override
    public void transform(ClassNode cn) {
        // Strip class annotations and metadata
        cn.access &= ~Opcodes.ACC_DEPRECATED;
        cn.access &= ~Opcodes.ACC_SYNTHETIC;
        cn.visibleAnnotations = null;
        cn.invisibleAnnotations = null;
        cn.visibleTypeAnnotations = null;
        cn.invisibleTypeAnnotations = null;
        cn.signature = null;
        cn.sourceFile = null;
        cn.sourceDebug = null;
        cn.outerClass = null;
        cn.outerMethod = null;
        cn.outerMethodDesc = null;

        // Remove debugging and synthetic info from methods
        for (MethodNode mn : cn.methods) {
            mn.access &= ~Opcodes.ACC_DEPRECATED;
            mn.access &= ~Opcodes.ACC_SYNTHETIC;
            mn.visibleAnnotations = null;
            mn.invisibleAnnotations = null;
            mn.visibleParameterAnnotations = null;
            mn.invisibleParameterAnnotations = null;
            mn.visibleTypeAnnotations = null;
            mn.invisibleTypeAnnotations = null;
            mn.exceptions = null;
            mn.signature = null;
            mn.localVariables = null;
            mn.tryCatchBlocks.clear();
            mn.instructions.resetLabels();
            mn.instructions.clear();
        }

        // Remove debugging and synthetic info from fields
        for (FieldNode fn : cn.fields) {
            fn.access &= ~Opcodes.ACC_DEPRECATED;
            fn.access &= ~Opcodes.ACC_SYNTHETIC;
            fn.visibleAnnotations = null;
            fn.invisibleAnnotations = null;
            fn.signature = null;
            fn.value = null;
        }
    }
}
