package com.evilcorp.fs;

import com.evilcorp.fs.FsFile;

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
