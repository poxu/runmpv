package com.evilcorp.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.LogManager;

public class Shortcuts {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static void initEmergencyLoggingSystem(String runmpvdir)  {
        try {
            LogManager.getLogManager().readConfiguration(
                new FileInputStream(runmpvdir)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAscii(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (string.charAt(i) > 127) {
                return false;
            }
        }
        return true;
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

    public static void createDirectoryIfNotExists(Path directory) {
        try {
            if (Files.notExists(directory)) {
                Files.createDirectory(directory);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
