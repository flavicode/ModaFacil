package com.utp.ModaFacil.util;

import java.nio.charset.StandardCharsets;

public class HexCrypter {

    private static String convertToHex(String text) {
        StringBuilder hexString = new StringBuilder();
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        
        for (byte b : bytes) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String encrypt(String text) {
        if (text == null) return null;

        String firstLayer = convertToHex(text);
        String secondLayer = convertToHex(firstLayer);
        return secondLayer;
    }

    public static boolean checkMatch(String text, String password) {
        if (text == null || password == null) return false;
        String inputHash = encrypt(text);
        return inputHash.equals(password);
    }
}
