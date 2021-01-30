package com.evilcorp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MpvRunnerProperties {
    private final short waitSeconds;
    private final String mpvHomeDir;
    private final String pipeName;
    private final String mpvLogFile;
    private final String executableDir;
    private final String runnerLogFile;

    public MpvRunnerProperties(
            final short waitSeconds,
            final String mpvHomeDir,
            final String pipeName,
            final String mpvLogFile,
            final String runnerLogFile
    ) {
        FsFile mpvRunnerHomeDir = new MpvRunnerExecutable();
        executableDir = mpvRunnerHomeDir.path().toString();
        this.runnerLogFile = runnerLogFile;
        this.waitSeconds = waitSeconds;
        this.mpvHomeDir = mpvHomeDir;
        this.pipeName = pipeName;
        this.mpvLogFile = mpvLogFile;
    }

    public MpvRunnerProperties(String fileName, FsFile mpvRunnerHomeDir, FsPaths fsPaths) {
        try {
            final Properties properties = new Properties();
            final String fullFileName = mpvRunnerHomeDir.path().toString() + "/" + fileName;
            final FileInputStream inStream = new FileInputStream(fullFileName);
            properties.load(inStream);
            executableDir = mpvRunnerHomeDir.path().toString();
            waitSeconds = Short.parseShort(properties.getProperty("waitSeconds"));
            runnerLogFile = fsPaths.resolve(properties.getProperty("runnerLogFile")).path()
                    .toString();
            mpvHomeDir = fsPaths.resolve(properties.getProperty("mpvHomeDir"))
                    .path().toString();
            pipeName = properties.getProperty("pipeName");
            mpvLogFile = fsPaths.resolve(properties.getProperty("mpvLogFile"))
                    .path().toString();
            inStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public short getWaitSeconds() {
        return waitSeconds;
    }

    public String getMpvHomeDir() {
        return mpvHomeDir;
    }

    public String getPipeName() {
        return pipeName;
    }

    public String getMpvLogFile() {
        return mpvLogFile;
    }

    public String getExecutableDir() {
        return executableDir;
    }

    public String getRunnerLogFile() {
        return runnerLogFile;
    }
}
