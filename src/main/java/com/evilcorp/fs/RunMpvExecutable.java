package com.evilcorp.fs;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * FsFile, representing runmpv.exe under Windows
 * or simply runmpv (without extension) under Linux
 * Uses code source location for finding executable location.
 */
public class RunMpvExecutable implements FsFile {
    private final Path executablePath;

    public RunMpvExecutable() {
        try {
            File executableFile = new File(RunMpvExecutable.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
            executablePath = executableFile.toPath().getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path path() {
        return executablePath;
    }
}
