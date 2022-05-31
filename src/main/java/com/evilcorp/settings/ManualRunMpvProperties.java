package com.evilcorp.settings;

@SuppressWarnings("MethodCount")
public class ManualRunMpvProperties implements RunMpvProperties {
    private final Integer waitSeconds;
    private final String mpvHomeDir;
    private final String pipeName;
    private final String mpvLogFile;
    private final String executableDir;
    private final String runnerLogFile;
    private final boolean focusAfterOpen;
    private final String video;

    public ManualRunMpvProperties(
        Integer waitSeconds,
        String mpvHomeDir,
        String pipeName,
        String mpvLogFile,
        String executableDir,
        String runnerLogFile,
        boolean focusAfterOpen,
        String video
    ) {
        this.waitSeconds = waitSeconds;
        this.mpvHomeDir = mpvHomeDir;
        this.pipeName = pipeName;
        this.mpvLogFile = mpvLogFile;
        this.executableDir = executableDir;
        this.runnerLogFile = runnerLogFile;
        this.focusAfterOpen = focusAfterOpen;
        this.video = video;
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

    @Override
    public boolean sync() {
        return false;
    }

    @Override
    public String syncAddress() {
        return "localhost";
    }

    @Override
    public int syncPort() {
        return 5445;
    }

    @Override
    public String video() {
        return video;
    }
}
