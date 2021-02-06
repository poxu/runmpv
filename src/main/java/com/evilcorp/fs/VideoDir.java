package com.evilcorp.fs;

import java.nio.file.Path;

/**
 * Represents directory, where video is located.
 * Is actually current working directory.
 * Encapsulates System.getProperty("user.dir")
 */
public class VideoDir implements FsFile {
    private final Path homeDir;

    public VideoDir() {
        homeDir = Path.of(System.getProperty("user.dir"));
    }

    @Override
    public Path path() {
        return homeDir;
    }
}
