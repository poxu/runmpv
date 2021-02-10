package com.evilcorp.settings;

import com.evilcorp.fs.FsPaths;

/**
 * Settings for runmpv.
 * Uses SoftSettings to take raw values and then
 * converts them to proper data types and
 * resolves paths if needed.
 */
public class MpvRunnerPropertiesFromSettings implements MpvRunnerProperties {
    private final FsPaths fsPaths;
    private final SoftSettings settings;

    public MpvRunnerPropertiesFromSettings(SoftSettings settings, FsPaths fsPaths) {
        this.fsPaths = fsPaths;
        this.settings = settings;
    }

    @Override
    public Integer waitSeconds() {
        return settings.setting("waitSeconds")
                .map(Integer::valueOf)
                .orElse(null);
    }

    @Override
    public String mpvHomeDir() {
        return settings.setting("mpvHomeDir")
                .map(s -> fsPaths.resolve(s).path().toString())
                .orElse(null);
    }

    @Override
    public String pipeName() {
        return settings.setting("pipeName")
                .orElse(null);
    }

    @Override
    public String mpvLogFile() {
        return settings.setting("mpvLogFile")
                .map(s -> fsPaths.resolve(s).path().toString())
                .orElse(null);
    }

    @Override
    public String executableDir() {
        return settings.setting("executableDir")
                .orElse(null);
    }

    @Override
    public String runnerLogFile() {
        return settings.setting("runnerLogFile")
                .map(s -> fsPaths.resolve(s).path().toString())
                .orElse(null);
    }

    @Override
    public boolean focusAfterOpen() {
        return settings.setting("focusAfterOpen")
                .map(Boolean::valueOf)
                .orElse(false);
    }
}
