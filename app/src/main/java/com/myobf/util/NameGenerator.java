package com.myobf.util;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class NameGenerator {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Random random = new SecureRandom();
    private final Set<String> usedNames = new HashSet<>();
    private final int length;

    public NameGenerator() {
        this(10); // Default length
    }

    public NameGenerator(int length) {
        this.length = length;
    }

    public String next() {
        String name;
        do {
            name = generateRandomString(length);
        } while (usedNames.contains(name));
        usedNames.add(name);
        return name;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
