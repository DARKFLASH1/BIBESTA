package com.example.BIBESTA.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    // Convertit un mot de passe en hash SHA-256
    // Exactement comme ton fichier SQL le fait
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // Convertit les bytes en chaîne hexadécimale
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hashage", e);
        }
    }
}