package com.evilcorp.mpv;

import com.evilcorp.fs.FsFile;
import com.evilcorp.fs.ManualFsFile;
import com.evilcorp.settings.SoftSettings;

import java.nio.file.Path;

/**
 * {@link RunmpvMinimalSettings} implementation, which takes settings from
 * {@link SoftSettings} .
 */
public class MinSettings implements RunmpvMinimalSettings {
    private final SoftSettings settings;

    public MinSettings(SoftSettings settings) {
        this.settings = settings;
    }

    /**
     * I am not sure if this will work with youtube links. I should check that.
     *
     * This is a perfect example, why computation should be lazy. Currently,
     * this parameter is only used to define socket names in case of
     * openMode=instance-per-directory. So videoDir is not used by default.
     *
     * But if Path::of does not work with youtube links, for example, then
     * runmpv will crash. Even though this setting is not used in any way.
     */
    @Override
    public FsFile videoDir() {
        return new ManualFsFile(
            settings.setting("videoFile").map(Path::of)
                .orElseThrow().getParent());
    }

    /**
     * Probably this parameter should be renamed from __executableDir__ to
     * __runmpvBinDir__ or __runmpvBin__
     */
    @Override
    public FsFile runmpvBinDir() {
        return settings.setting("executableDir")
            .map(dir -> (FsFile)new ManualFsFile(Path.of(dir)))
            .orElseThrow();
    }
}
