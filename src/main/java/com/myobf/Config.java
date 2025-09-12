package com.myobf;

import com.google.gson.Gson;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {

    // Global toggles
    public boolean rename = true;
    public boolean stringEncryption = true;
    public boolean integerEncryption = true;
    public boolean controlFlow = true;

    // Specific features
    public boolean illegalBytecode = false;
    public boolean virtualization = false;
    public boolean watermark = true;
    public boolean customClassLoader = false;
    public boolean antiDebug = false;
    public boolean polymorphic = false;
    public boolean nativeStubs = false;
    public boolean garbageInjection = true;
    public boolean jitObfuscation = false;
    public boolean lambdaTransformer = true;
    public boolean annotationStripper = true;
    public boolean reflectionIndirection = false;
    public boolean constantPoolTransform = false;
    public boolean resourceBundler = false;
    public boolean tryCatchObfuscator = true;
    public boolean garbageClassGenerator = true;
    public boolean buildFingerprint = false;
    public boolean licenseGate = false;
    public boolean telemetryProtector = false;
    public boolean packingTransformer = false;
    public boolean integritySampler = false;
    public boolean obfuscationReport = true;

    // Default constructor for default settings
    public Config() {}

    public static Config load(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return new Config(); // Return default config if no path is provided
        }
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            Gson gson = new Gson();
            return gson.fromJson(reader, Config.class);
        } catch (Exception e) {
            System.err.println("Warning: Could not load or parse config file. Using default settings. Error: " + e.getMessage());
            return new Config(); // Return default on error
        }
    }
}
