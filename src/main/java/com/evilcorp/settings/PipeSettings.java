package com.evilcorp.settings;

import java.util.Optional;

/**
 * Settings implementation, which overrides pipeName setting,
 * depending on openMode.
 */
public class PipeSettings implements SoftSettings {
    private final SoftSettings pipePerDirectorySettings;
    private final SoftSettings openModeSettings;

    public PipeSettings(SoftSettings pipePerDirectorySettings, SoftSettings openModeSettings) {
        this.pipePerDirectorySettings = pipePerDirectorySettings;
        this.openModeSettings = openModeSettings;
    }

    @Override
    public Optional<String> setting(String name) {
        return openModeSettings.setting(name)
            .filter(e -> "pipeName".equals(name))
            .filter(e -> openModeSettings.setting("openMode").orElseThrow()
                .equals("instance-per-directory"))
            .map(e -> pipePerDirectorySettings.setting(name))
            .orElse(openModeSettings.setting(name));
    }
}
