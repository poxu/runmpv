package com.evilcorp.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Shortcuts {
    public static void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static InputStream getInStream(String propertyFileName) {
        try {
            return new FileInputStream(propertyFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String bytesToHex(byte[] hash) {
        final StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            final String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
