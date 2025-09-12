package com.myobf;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "myobf", mixinStandardHelpOptions = true, version = "MyObf 1.0",
        description = "A powerful Java obfuscator.")
public class MyObf implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, required = true, description = "The input JAR file.")
    private File inputFile;

    @Option(names = {"-o", "--output"}, required = true, description = "The output JAR file.")
    private File outputFile;

    @Option(names = {"-c", "--config"}, description = "The configuration JSON file.")
    private File configFile;

    @Override
    public Integer call() throws Exception {
        System.out.println("MyObf - Java Obfuscator");
        System.out.println("========================");
        System.out.printf("Input JAR: %s%n", inputFile.getAbsolutePath());
        System.out.printf("Output JAR: %s%n", outputFile.getAbsolutePath());

        if (configFile != null) {
            System.out.printf("Config file: %s%n", configFile.getAbsolutePath());
        } else {
            System.out.println("No config file provided. Using default settings.");
        }

        try {
            // Load configuration
            Config config = Config.load(configFile != null ? configFile.getAbsolutePath() : null);

            // Initialize and run the JarProcessor
            JarProcessor processor = new JarProcessor(inputFile, outputFile, config);
            processor.run();

            System.out.println("\nObfuscation process completed successfully.");
            return 0;
        } catch (Exception e) {
            System.err.println("An error occurred during obfuscation: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MyObf()).execute(args);
        System.exit(exitCode);
    }
}
