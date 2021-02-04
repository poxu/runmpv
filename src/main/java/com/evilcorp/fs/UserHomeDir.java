package com.evilcorp.fs;

import java.nio.file.Path;

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
