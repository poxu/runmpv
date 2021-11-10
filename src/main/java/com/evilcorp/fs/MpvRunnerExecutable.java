package com.evilcorp.fs;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * FsFile, representing runmpv.exe
 * Uses code source location for find executable location.
 */
public class MpvRunnerExecutable implements FsFile {
    private final Path executablePath;

    public MpvRunnerExecutable() {
        try {
            File executableFile = new File(MpvRunnerExecutable.class.getProtectionDomain().getCodeSource().getLocation()
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
