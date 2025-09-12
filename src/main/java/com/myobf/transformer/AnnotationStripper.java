package com.myobf.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class AnnotationStripper implements Transformer {

    @Override
    public void transform(ClassNode cn) {
        // Remove the deprecated flag and all annotations from the class
        cn.access &= ~Opcodes.ACC_DEPRECATED;
        cn.visibleAnnotations = null;
        cn.invisibleAnnotations = null;

        // Remove the deprecated flag and all annotations from methods
        for (MethodNode mn : cn.methods) {
            mn.access &= ~Opcodes.ACC_DEPRECATED;
            mn.visibleAnnotations = null;
            mn.invisibleAnnotations = null;
            mn.visibleParameterAnnotations = null;
            mn.invisibleParameterAnnotations = null;
        }

        // Remove the deprecated flag and all annotations from fields
        for (FieldNode fn : cn.fields) {
            fn.access &= ~Opcodes.ACC_DEPRECATED;
            fn.visibleAnnotations = null;
            fn.invisibleAnnotations = null;
        }
    }
}
