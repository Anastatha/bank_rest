package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CardCryptoUtil {

    private final SecretKeySpec secretKeySpec;

    public CardCryptoUtil(@Value("${app.card.aes-key}") String aesKey) {
        byte[] keyBytes = adjustKeyLength(aesKey);
        this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] adjustKeyLength(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32) {
            return keyBytes;
        }

        if (keyBytes.length < 16) {
            byte[] padded = new byte[16];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return padded;
        } else if (keyBytes.length < 24) {
            byte[] padded = new byte[24];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 24));
            return padded;
        } else if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            return padded;
        } else {
            byte[] trimmed = new byte[32];
            System.arraycopy(keyBytes, 0, trimmed, 0, 32);
            return trimmed;
        }
    }

    public String encrypt(String plain) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number: " + e.getMessage(), e);
        }
    }

    public String decrypt(String base64) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decoded = Base64.getDecoder().decode(base64);
            byte[] original = cipher.doFinal(decoded);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number: " + e.getMessage(), e);
        }
    }

    public static String mask(String plainNumber) {
        if (plainNumber == null) return null;
        String digits = plainNumber.replaceAll("\\s+", "");
        if (digits.length() <= 4) return digits;
        String last4 = digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }
}