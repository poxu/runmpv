package com.evilcorp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class MpvRunnerProperties {
    private final short waitSeconds;
    private final String mpvHomeDir;
    private final String pipeName;
    private final String mpvLogFile;
    private final String executableDir;
    private final String executable;
    private final String runnerLogFile;

    public MpvRunnerProperties(
            final short waitSeconds,
            final String mpvHomeDir,
            final String pipeName,
            final Class<?> startSingleMpvInstanceClass,
            final String mpvLogFile,
            final String runnerLogFile
    ) {
        final File executableFile;
        try {
            executableFile = new File(startSingleMpvInstanceClass.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        executableDir = executableFile.getPath();
        executable = executableFile.getParentFile().getPath();
        this.runnerLogFile = runnerLogFile;
        this.waitSeconds = waitSeconds;
        this.mpvHomeDir = mpvHomeDir;
        this.pipeName = pipeName;
        this.mpvLogFile = mpvLogFile;
    }

    public MpvRunnerProperties(Class<?> startSingleMpvInstanceClass, String fileName) {
        try {
            final File executableFile = new File(startSingleMpvInstanceClass.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            final Properties properties = new Properties();
            final String fullFileName = executableFile.getParent() + "/" + fileName;
            final FileInputStream inStream = new FileInputStream(fullFileName);
            properties.load(inStream);
            executableDir = executableFile.getPath();
            executable = executableFile.getParentFile().getPath();
            runnerLogFile = properties.getProperty("runnerLogFile");
            waitSeconds = Short.parseShort(properties.getProperty("waitSeconds"));
            mpvHomeDir = properties.getProperty("mpvHomeDir");
            pipeName = properties.getProperty("pipeName");
            mpvLogFile = properties.getProperty("mpvLogFile");
            inStream.close();
        } catch (URISyntaxException | IOException e) {
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
