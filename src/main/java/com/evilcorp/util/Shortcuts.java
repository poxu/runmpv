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
}
