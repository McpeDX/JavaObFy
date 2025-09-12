package com.myobf.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {

    private static final SecretKey SECRET_KEY;
    private static final IvParameterSpec IV_PARAMETER_SPEC;

    static {
        try {
            // Generate a new random 128-bit AES key for each obfuscation run
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SECRET_KEY = keyGen.generateKey();

            // Generate a random 16-byte IV
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IV_PARAMETER_SPEC = new IvParameterSpec(iv);
        } catch (NoSuchAlgorithmException e) {
            // This should never happen with a standard Java environment
            throw new RuntimeException("AES algorithm not found", e);
        }
    }

    public static SecretKey getKey() {
        return SECRET_KEY;
    }

    public static byte[] getIv() {
        return IV_PARAMETER_SPEC.getIV();
    }

    public static String encrypt(String plainText, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            // Wrap crypto exceptions in a runtime exception
            throw new RuntimeException("Failed to encrypt string", e);
        }
    }
}
