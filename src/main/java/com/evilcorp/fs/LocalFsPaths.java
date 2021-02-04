package com.evilcorp.fs;

import java.nio.file.Path;

public class LocalFsPaths implements FsPaths {
    private final FsFile homeDir;
    private final FsFile mpvRunnerDir;
    private final FsFile videoDir;

    public LocalFsPaths(FsFile homeDir, FsFile mpvRunnerDir, FsFile videoDir) {
        this.homeDir = homeDir;
        this.mpvRunnerDir = mpvRunnerDir;
        this.videoDir = videoDir;
    }

    @Override
    public FsFile resolve(String path) {
        if (path.charAt(0) != '%') {
            return new ManualFsFile(Path.of(path));
        }
        FsFile relativeTo;
        if (path.charAt(1) == 'h') {
            relativeTo = homeDir;
        } else if (path.charAt(1) == 'r') {
            relativeTo = mpvRunnerDir;
        } else if (path.charAt(1) == 'v') {
            relativeTo = videoDir;
        } else {
            return new ManualFsFile(Path.of(path));
        }
        return new ManualFsFile(relativeTo.path().resolve("./" + path.substring(2)).normalize());
    }
}
