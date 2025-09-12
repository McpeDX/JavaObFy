package com.sample;

public class Main {
    private static final String GREETING = "Hello, World!";

    public static void main(String[] args) {
        Greeter greeter = new Greeter();
        greeter.greet(GREETING);
    }
}

class Greeter {
    public void greet(String message) {
        System.out.println(message);
    }
}
