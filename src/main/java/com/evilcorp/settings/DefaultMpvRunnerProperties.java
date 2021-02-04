package com.evilcorp.settings;

import com.evilcorp.fs.FsPaths;

public class DefaultMpvRunnerProperties implements MpvRunnerProperties {
    private final FsPaths fsPaths;

    public DefaultMpvRunnerProperties(FsPaths fsPaths) {
        this.fsPaths = fsPaths;
    }

    @Override
    public Integer waitSeconds() {
        return 5;
    }

    @Override
    public String mpvHomeDir() {
        return fsPaths.resolve("%r/..")
                .path().toString();
    }

    @Override
    public String pipeName() {
        return "runmpv-mpv-pipe";
    }

    @Override
    public String mpvLogFile() {
        return null;
    }

    @Override
    public String executableDir() {
        return fsPaths.resolve("%r").path()
                .toString();

    }

    @Override
    public String runnerLogFile() {
        return null;
    }
}
