package com.myobf.name;

import java.util.HashSet;
import java.util.Set;

public class NameGenerator {

    private int classCounter = 0;
    private int methodCounter = 0;
    private int fieldCounter = 0;
    private final Set<String> usedNames = new HashSet<>();

    public NameGenerator() {
        // Reserve Java keywords
        usedNames.addAll(Set.of(
            "abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized",
            "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw",
            "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient",
            "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void",
            "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while",
            // Reserved literals
            "true", "false", "null"
        ));
    }

    private String generateName(int counter) {
        if (counter < 0) {
            throw new IllegalArgumentException("Counter cannot be negative");
        }
        StringBuilder sb = new StringBuilder();
        int current = counter;
        if (current == 0) {
            return "a";
        }
        while (current >= 0) {
            sb.insert(0, (char) ('a' + (current % 26)));
            current = (current / 26) - 1;
            if (current < 0) break;
        }
        return sb.toString();
    }

    public String newClassName() {
        String name;
        do {
            name = generateName(classCounter++);
        } while (usedNames.contains(name));
        usedNames.add(name);
        return name;
    }

    public String newMethodName() {
        String name;
        do {
            name = generateName(methodCounter++);
        } while (usedNames.contains(name));
        usedNames.add(name);
        return name;
    }

    public String newFieldName() {
        String name;
        do {
            name = generateName(fieldCounter++);
        } while (usedNames.contains(name));
        usedNames.add(name);
        return name;
    }
}
