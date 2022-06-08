package com.evilcorp.settings;

import com.evilcorp.fs.FsFile;

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
    private final String runmpvTmpDir;
    private final FsFile videoDir;
    private final FsFile runmpvBinDir;
    private final FsFile logSettings;
    private final FsFile userHome;

    public ManualRunMpvProperties(
        Integer waitSeconds,
        String mpvHomeDir,
        String pipeName,
        String mpvLogFile,
        String executableDir,
        String runnerLogFile,
        boolean focusAfterOpen,
        String video,
        String runmpvTmpDir,
        FsFile videoDir,
        FsFile runmpvBinDir,
        FsFile logSettings,
        FsFile userHome) {
        this.waitSeconds = waitSeconds;
        this.mpvHomeDir = mpvHomeDir;
        this.pipeName = pipeName;
        this.mpvLogFile = mpvLogFile;
        this.executableDir = executableDir;
        this.runnerLogFile = runnerLogFile;
        this.focusAfterOpen = focusAfterOpen;
        this.video = video;
        this.runmpvTmpDir = runmpvTmpDir;
        this.videoDir = videoDir;
        this.runmpvBinDir = runmpvBinDir;
        this.logSettings = logSettings;
        this.userHome = userHome;
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

    @Override
    public String runmpvTmpDir() {
        return runmpvTmpDir;
    }

    @Override
    public FsFile videoDir() {
        return videoDir;
    }

    @Override
    public FsFile runmpvBinDir() {
        return runmpvBinDir;
    }

    @Override
    public FsFile logSettings() {
        return logSettings;
    }

    @Override
    public FsFile userHome() {
        return userHome;
    }
}
