package com.myobf.name;

import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renamer {

    private final NameGenerator nameGenerator = new NameGenerator();
    private final Map<String, String> mapping = new HashMap<>();

    public SimpleRemapper buildRemapping(List<ClassNode> classNodes) {
        // First, map all class names
        for (ClassNode cn : classNodes) {
            String oldName = cn.name;
            String newName = nameGenerator.newClassName();
            mapping.put(oldName, newName);
        }

        // Then, map all method and field names, using the new class names for keys
        for (ClassNode cn : classNodes) {
            String oldClassName = cn.name;
            String newClassName = mapping.get(oldClassName);

            // Map methods
            for (MethodNode mn : cn.methods) {
                // Do not rename constructors or the main method
                if (mn.name.equals("<init>") || mn.name.equals("<clinit>") || isMainMethod(mn)) {
                    continue;
                }
                String oldMethodKey = oldClassName + "." + mn.name + mn.desc;
                String newMethodName = nameGenerator.newMethodName();
                mapping.put(oldMethodKey, newMethodName);
            }

            // Map fields
            cn.fields.forEach(fn -> {
                String oldFieldKey = oldClassName + "." + fn.name;
                String newFieldName = nameGenerator.newFieldName();
                mapping.put(oldFieldKey, newFieldName);
            });
        }

        return new SimpleRemapper(mapping);
    }

    private boolean isMainMethod(MethodNode mn) {
        return mn.name.equals("main") && mn.desc.equals("([Ljava/lang/String;)V")
                && (mn.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0
                && (mn.access & org.objectweb.asm.Opcodes.ACC_PUBLIC) != 0;
    }
}
