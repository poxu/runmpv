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
            final String executableDir,
            final short waitSeconds,
            final String mpvHomeDir,
            final String pipeName,
            final String mpvLogFile,
            final String runnerLogFile
    ) {
        this.executableDir = executableDir;
        this.runnerLogFile = runnerLogFile;
        this.waitSeconds = waitSeconds;
        this.mpvHomeDir = mpvHomeDir;
        this.pipeName = pipeName;
        this.mpvLogFile = mpvLogFile;
    }

    public MpvRunnerProperties(String propertyFileName, FsPaths fsPaths) {
        try {
            final String fullFileName = fsPaths.resolve("%r/" + propertyFileName).path()
                    .toString();
            final FileInputStream inStream = new FileInputStream(fullFileName);
            final Properties properties = new Properties();
            properties.load(inStream);
            inStream.close();
            executableDir = fsPaths.resolve("%r").path()
                    .toString();
            waitSeconds = Short.parseShort(properties.getProperty("waitSeconds"));
            runnerLogFile = fsPaths.resolve(properties.getProperty("runnerLogFile")).path()
                    .toString();
            mpvHomeDir = fsPaths.resolve(properties.getProperty("mpvHomeDir"))
                    .path().toString();
            pipeName = properties.getProperty("pipeName");
            mpvLogFile = fsPaths.resolve(properties.getProperty("mpvLogFile"))
                    .path().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public short waitSeconds() {
        return waitSeconds;
    }

    public String mpvHomeDir() {
        return mpvHomeDir;
    }

    public String pipeName() {
        return pipeName;
    }

    public String mpvLogFile() {
        return mpvLogFile;
    }

    public String executableDir() {
        return executableDir;
    }

    public String runnerLogFile() {
        return runnerLogFile;
    }
}
