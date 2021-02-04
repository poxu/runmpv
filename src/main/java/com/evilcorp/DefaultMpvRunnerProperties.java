package com.evilcorp;

public class DefaultMpvRunnerProperties implements MpvRunnerProperties {
    private final FsPaths fsPaths;

    public DefaultMpvRunnerProperties(FsPaths fsPaths) {
        this.fsPaths = fsPaths;
    }

    @Override
    public short waitSeconds() {
        return 5;
    }

    @Override
    public String mpvHomeDir() {
        return fsPaths.resolve("%h/soft/mpv")
                .path().toString();
    }

    @Override
    public String pipeName() {
        return "runmpv-mpv-pipe";
    }

    @Override
    public String mpvLogFile() {
        return fsPaths.resolve("%r/debug.log")
                .path().toString();
    }

    @Override
    public String executableDir() {
        return fsPaths.resolve("%r").path()
                .toString();

    }

    @Override
    public String runnerLogFile() {
        return fsPaths.resolve("%r/runner-debug.log").path()
                .toString();
    }
}
