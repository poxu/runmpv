package com.evilcorp;

public class ManualMpvRunnerProperties implements MpvRunnerProperties {
    private final short waitSeconds;
    private final String mpvHomeDir;
    private final String pipeName;
    private final String mpvLogFile;
    private final String executableDir;
    private final String runnerLogFile;

    public ManualMpvRunnerProperties(
            short waitSeconds,
            String mpvHomeDir,
            String pipeName,
            String mpvLogFile,
            String executableDir,
            String runnerLogFile
    ) {
        this.waitSeconds = waitSeconds;
        this.mpvHomeDir = mpvHomeDir;
        this.pipeName = pipeName;
        this.mpvLogFile = mpvLogFile;
        this.executableDir = executableDir;
        this.runnerLogFile = runnerLogFile;
    }

    @Override
    public short waitSeconds() {
        return waitSeconds;
    }

    @Override
    public String mpvHomeDir() {
        return mpvHomeDir;
    }

    @Override
    public String pipeName() {
        return pipeName;
    }

    @Override
    public String mpvLogFile() {
        return mpvLogFile;
    }

    @Override
    public String executableDir() {
        return executableDir;
    }

    @Override
    public String runnerLogFile() {
        return runnerLogFile;
    }
}
