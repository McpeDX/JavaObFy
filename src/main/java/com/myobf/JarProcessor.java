package com.myobf;

import com.myobf.name.Renamer;
import com.myobf.transformer.ArithmeticTransformer;
import com.myobf.transformer.DeadCodeInjector;
import com.myobf.transformer.StringEncryptTransformer;
import com.myobf.transformer.AnnotationStripper;
import com.myobf.transformer.Transformer;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarProcessor {

    private final File inputFile;
    private final File outputFile;
    private final Config config;
    private final List<Transformer> localTransformers = new ArrayList<>();

    public JarProcessor(File inputFile, File outputFile, Config config) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.config = config;
    }

    public void run() throws Exception {
        // 1. Read all classes into memory
        List<ClassNode> classNodes = readAllClasses();

        // 2. Renaming Phase
        if (config.rename) {
            Renamer renamer = new Renamer();
            Remapper remapper = renamer.buildRemapping(classNodes);
            classNodes = remapClasses(classNodes, remapper);
        }

        // 3. Global Data Obfuscation Phase
        if (config.stringEncryption) {
            new StringEncryptTransformer().transform(classNodes);
        }
        if (config.integerEncryption) {
            new com.myobf.transformer.IntegerEncryptTransformer().transform(classNodes);
        }

        // 4. Local Transformations Phase
        initializeLocalTransformers();
        for (ClassNode classNode : classNodes) {
            for (Transformer transformer : localTransformers) {
                transformer.transform(classNode);
            }
        }

        // 5. Write all classes and resources to the output JAR
        writeOutputJar(classNodes);
    }

    private List<ClassNode> readAllClasses() throws IOException {
        List<ClassNode> classNodes = new ArrayList<>();
        try (JarFile jarFile = new JarFile(inputFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        ClassReader classReader = new ClassReader(is);
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
                        classNodes.add(classNode);
                    }
                }
            }
        }
        return classNodes;
    }

    private List<ClassNode> remapClasses(List<ClassNode> originalClassNodes, Remapper remapper) {
        List<ClassNode> remappedClassNodes = new ArrayList<>();
        for (ClassNode cn : originalClassNodes) {
            ClassNode remappedCn = new ClassNode();
            ClassRemapper classRemapper = new ClassRemapper(remappedCn, remapper);
            cn.accept(classRemapper);
            remappedClassNodes.add(remappedCn);
        }
        return remappedClassNodes;
    }

    private void initializeLocalTransformers() {
        if (config.annotationStripper) {
            localTransformers.add(new AnnotationStripper());
        }
        if (config.controlFlow) {
            localTransformers.add(new DeadCodeInjector());
            localTransformers.add(new ArithmeticTransformer());
        }
    }

    private void writeOutputJar(List<ClassNode> classNodes) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputFile))) {
            for (ClassNode classNode : classNodes) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                classNode.accept(classWriter);
                JarEntry newEntry = new JarEntry(classNode.name + ".class");
                jos.putNextEntry(newEntry);
                jos.write(classWriter.toByteArray());
                jos.closeEntry();
            }

            try (JarFile jarFile = new JarFile(inputFile)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(".class")) {
                        jos.putNextEntry(new JarEntry(entry.getName()));
                        try (InputStream is = jarFile.getInputStream(entry)) {
                            jos.write(IOUtils.toByteArray(is));
                        }
                        jos.closeEntry();
                    }
                }
            }
        }
    }
}
