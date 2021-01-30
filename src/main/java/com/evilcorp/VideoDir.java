package com.evilcorp;

import java.nio.file.Path;

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
