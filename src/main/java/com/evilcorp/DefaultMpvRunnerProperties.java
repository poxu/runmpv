package com.evilcorp;

public class DefaultMpvRunnerProperties implements MpvRunnerProperties {
    private final short waitSeconds;
    private final String mpvHomeDir;
    private final String pipeName;
    private final String mpvLogFile;
    private final String executableDir;
    private final String runnerLogFile;

    public DefaultMpvRunnerProperties(FsPaths fsPaths) {
            executableDir = fsPaths.resolve("%r").path()
                    .toString();
            waitSeconds = 5;
            runnerLogFile = fsPaths.resolve("%r/runner-debug.log").path()
                    .toString();
            mpvHomeDir = fsPaths.resolve("%h/soft/mpv")
                    .path().toString();
            pipeName = "runmpv-mpv-pipe";
            mpvLogFile = fsPaths.resolve("%r/debug.log")
                    .path().toString();
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
