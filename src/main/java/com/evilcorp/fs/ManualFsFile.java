package com.evilcorp.fs;

import java.nio.file.Path;

public class ManualFsFile implements FsFile {
    private final Path path;

    public ManualFsFile(Path path) {
        this.path = path;
    }

    @Override
    public Path path() {
        return path;
    }
}
