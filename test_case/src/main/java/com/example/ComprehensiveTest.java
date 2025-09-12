package com.example;

@Deprecated
public class ComprehensiveTest {

    private static final int INT_CONST_1 = 12345;
    private static final int INT_CONST_2 = 5;
    private static final String GREETING = "Hello, World!";
    private static final String FAREWELL = "Goodbye!";

    public static void main(String[] args) {
        System.out.println("--- Comprehensive Test Start ---");

        // Test String Encryption
        System.out.println(GREETING);
        System.out.println(FAREWELL);

        // Test Integer Encryption
        int localInt1 = 99;
        int localInt2 = INT_CONST_1;
        System.out.println("Local Int 1: " + localInt1);
        System.out.println("Local Int 2: " + localInt2);

        // Test Arithmetic Obfuscation
        int a = 100;
        int b = 50;

        int add = a + b; // IADD
        System.out.println("Addition (100+50): " + add);

        int sub = a - b; // ISUB
        System.out.println("Subtraction (100-50): " + sub);

        int xor = a ^ b; // IXOR
        System.out.println("XOR (100^50): " + xor);

        int and = 12 & 6; // IAND
        System.out.println("AND (12&6): " + and);

        int or = 12 | 6; // IOR
        System.out.println("OR (12|6): " + or);

        System.out.println("--- Comprehensive Test End ---");
    }

    @Deprecated
    public void unusedMethod() {
        // This method is here to test renaming and dead code injection.
    }
}
