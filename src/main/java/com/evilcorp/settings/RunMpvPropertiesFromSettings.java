package com.evilcorp.settings;

import com.evilcorp.fs.FsFile;
import com.evilcorp.fs.FsPaths;
import com.evilcorp.mpv.MinSettings;
import com.evilcorp.mpv.RunmpvMinimalSettings;

/**
 * Settings for runmpv.
 * Uses SoftSettings to take raw values and then
 * converts them to proper data types and
 * resolves paths if needed.
 */
@SuppressWarnings("MethodCount")
public class RunMpvPropertiesFromSettings implements RunMpvProperties, RunmpvMinimalSettings {
    private final FsPaths fsPaths;
    private final SoftSettings settings;
    private final RunmpvMinimalSettings minSettings;

    public RunMpvPropertiesFromSettings(SoftSettings settings, FsPaths fsPaths) {
        this.fsPaths = fsPaths;
        this.settings = settings;
        this.minSettings = new MinSettings(settings);
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
        return minSettings.runmpvBinDir().path().toString();
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

    @Override
    public boolean sync() {
        return settings.setting("sync")
            .map(Boolean::valueOf)
            .orElse(false);
    }

    @Override
    public String syncAddress() {
        return settings.setting("syncAddress")
            .orElse("localhost");
    }

    @Override
    public int syncPort() {
        return settings.setting("syncPort")
            .map(Integer::valueOf)
            .orElse(5454);
    }

    @Override
    public String video() {
        return settings.setting("videoFile")
            .orElse(null);
    }

    @Override
    public String runmpvTmpDir() {
        return settings.setting("runmpvTmpDir")
            .orElse(null);
    }

    @Override
    public FsFile videoDir() {
        return minSettings.videoDir();
    }

    @Override
    public FsFile runmpvBinDir() {
        return minSettings.runmpvBinDir();
    }

    @Override
    public FsFile logSettings() {
        return minSettings.logSettings();
    }

    @Override
    public FsFile userHome() {
        return minSettings.userHome();
    }
}
