package com.evilcorp.fs;

import java.nio.file.Path;

/**
 * Able to resolve placeholders standing in the beginning
 * of the path.
 * <p>
 * %r - directory where runmpv is located
 * %h - user home directory
 * %v - directory, containing video file, currently played
 */
public class LocalFsPaths implements FsPaths {
    private final FsFile homeDir;
    private final FsFile runMpvDir;
    private final FsFile videoDir;

    public LocalFsPaths(
        final FsFile homeDir,
        final FsFile runMpvDir,
        final FsFile videoDir
    ) {
        this.homeDir = homeDir;
        this.runMpvDir = runMpvDir;
        this.videoDir = videoDir;
    }

    @Override
    public FsFile resolve(String path) {
        if (!path.startsWith("%")) {
            return new ManualFsFile(Path.of(path));
        }
        final char directoryPlaceholder = path.charAt(1);
        final FsFile relativeTo = switch (directoryPlaceholder) {
            case 'h' -> homeDir;
            case 'r' -> runMpvDir;
            case 'v' -> videoDir;
            default -> new ManualFsFile(Path.of(path));
        };
        return new ManualFsFile(relativeTo.path()
            .resolve("./" + path.substring(2)).normalize());
    }
}
