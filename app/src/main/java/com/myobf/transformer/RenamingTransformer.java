package com.myobf.transformer;

import com.myobf.util.NameGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RenamingTransformer {

    private final Map<String, String> remapping = new HashMap<>();
    private final NameGenerator classNameGenerator = new NameGenerator();
    private final NameGenerator methodNameGenerator = new NameGenerator();
    private final NameGenerator fieldNameGenerator = new NameGenerator();

    public void analyze(Collection<ClassNode> classNodes) {
        // First pass: generate new names for all classes, methods, and fields
        for (ClassNode cn : classNodes) {
            // Rename class
            String newClassName = classNameGenerator.next();
            remapping.put(cn.name, newClassName);

            // Rename methods
            for (MethodNode mn : cn.methods) {
                if (!mn.name.equals("<init>") && !mn.name.equals("<clinit>")) { // Don't rename constructors
                    String oldMethodName = cn.name + "." + mn.name + mn.desc;
                    String newMethodName = methodNameGenerator.next();
                    remapping.put(oldMethodName, newMethodName);
                }
            }

            // Rename fields
            for (FieldNode fn : cn.fields) {
                String oldFieldName = cn.name + "." + fn.name;
                String newFieldName = fieldNameGenerator.next();
                remapping.put(oldFieldName, newFieldName);
            }
        }
    }

    public Map<String, ClassNode> transform(Collection<ClassNode> classNodes) {
        Remapper remapper = new SimpleRemapper(remapping) {
            @Override
            public String mapMethodName(String owner, String name, String desc) {
                String key = owner + "." + name + desc;
                String remappedName = remapping.get(key);
                return remappedName != null ? remappedName : name;
            }

            @Override
            public String mapFieldName(String owner, String name, String desc) {
                String key = owner + "." + name;
                String remappedName = remapping.get(key);
                return remappedName != null ? remappedName : name;
            }
        };

        Map<String, ClassNode> transformedClasses = new HashMap<>();

        for (ClassNode cn : classNodes) {
            ClassWriter cw = new ClassWriter(0);
            ClassRemapper cr = new ClassRemapper(cw, remapper);
            cn.accept(cr);

            // We need to read the transformed class back into a ClassNode
            ClassReader reader = new ClassReader(cw.toByteArray());
            ClassNode transformedNode = new ClassNode();
            reader.accept(transformedNode, 0);

            transformedClasses.put(transformedNode.name, transformedNode);
        }

        return transformedClasses;
    }
}
