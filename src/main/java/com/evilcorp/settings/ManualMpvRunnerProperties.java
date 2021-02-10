package com.evilcorp.settings;

public class ManualMpvRunnerProperties implements MpvRunnerProperties {
    private final Integer waitSeconds;
    private final String mpvHomeDir;
    private final String pipeName;
    private final String mpvLogFile;
    private final String executableDir;
    private final String runnerLogFile;
    private final boolean focusAfterOpen;

    public ManualMpvRunnerProperties(
            Integer waitSeconds,
            String mpvHomeDir,
            String pipeName,
            String mpvLogFile,
            String executableDir,
            String runnerLogFile,
            boolean focusAfterOpen) {
        this.waitSeconds = waitSeconds;
        this.mpvHomeDir = mpvHomeDir;
        this.pipeName = pipeName;
        this.mpvLogFile = mpvLogFile;
        this.executableDir = executableDir;
        this.runnerLogFile = runnerLogFile;
        this.focusAfterOpen = focusAfterOpen;
    }

    @Override
    public Integer waitSeconds() {
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

    @Override
    public boolean focusAfterOpen() {
        return focusAfterOpen;
    }
}
