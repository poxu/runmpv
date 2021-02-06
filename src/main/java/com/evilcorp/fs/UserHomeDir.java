package com.evilcorp.fs;

import java.nio.file.Path;

/**
 * Represents user home directory.
 * Encapsulates System.getProperty("user.home")
 */
public class UserHomeDir implements FsFile {
    private final Path homeDir;

    public UserHomeDir() {
        homeDir = Path.of(System.getProperty("user.home"));
    }

    @Override
    public Path path() {
        return homeDir;
    }
}
