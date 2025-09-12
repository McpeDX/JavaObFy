package com.myobf;

import com.myobf.model.Configuration;
import com.myobf.util.ConfigLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import org.objectweb.asm.ClassWriter;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class MyObf {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java -jar myobf.jar <input.jar> <output.jar> <config.json>");
            return;
        }

        String inputJarPath = args[0];
        String outputJarPath = args[1];
        String configPath = args[2];

        try {
            System.out.println("Loading configuration from: " + configPath);
            Configuration config = ConfigLoader.loadConfig(configPath);
            System.out.println("Configuration loaded successfully.");

            System.out.println("Reading input JAR: " + inputJarPath);
            Map<String, ClassNode> classNodes = loadClasses(new File(inputJarPath));
            System.out.println("Loaded " + classNodes.size() + " classes.");

            if (config.isRename()) {
                System.out.println("Applying renaming transformation...");
                com.myobf.transformer.RenamingTransformer renamer = new com.myobf.transformer.RenamingTransformer();
                renamer.analyze(classNodes.values());
                classNodes = renamer.transform(classNodes.values());
                System.out.println("Renaming transformation applied.");
            }

            // More logic will be added here for writing the output JAR.
            System.out.println("Writing output JAR: " + outputJarPath);
            writeJar(classNodes, new File(inputJarPath), outputJarPath);
            System.out.println("Obfuscation complete.");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeJar(Map<String, ClassNode> classNodes, File originalJar, String outputJarPath) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJarPath))) {
            // Write transformed classes
            for (ClassNode cn : classNodes.values()) {
                JarEntry entry = new JarEntry(cn.name + ".class");
                jos.putNextEntry(entry);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cn.accept(cw);
                jos.write(cw.toByteArray());
                jos.closeEntry();
            }

            // Copy resources from original jar
            try (JarFile jar = new JarFile(originalJar)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(".class")) {
                        jos.putNextEntry(entry);
                        try (InputStream is = jar.getInputStream(entry)) {
                            jos.write(is.readAllBytes());
                        }
                        jos.closeEntry();
                    }
                }
            }
        }
    }

    private static Map<String, ClassNode> loadClasses(File jarFile) throws IOException {
        Map<String, ClassNode> classNodes = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        ClassReader classReader = new ClassReader(is);
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, 0);
                        classNodes.put(classNode.name, classNode);
                    }
                }
            }
        }
        return classNodes;
    }
}
