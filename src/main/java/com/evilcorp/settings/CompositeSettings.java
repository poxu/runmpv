package com.evilcorp.settings;

import java.util.Optional;

/**
 * Settings, which compose actual settings and
 * default settings. Values are taken from actual settings
 * first and if values is not found, default settings
 * are searched.
 */
public class CompositeSettings implements SoftSettings {
    private final SoftSettings actualSettings;
    private final SoftSettings defaultSettings;

    public CompositeSettings(
        SoftSettings actualSettings,
        SoftSettings defaultSettings
    ) {
        this.actualSettings = actualSettings;
        this.defaultSettings = defaultSettings;
    }

    @Override
    public Optional<String> setting(String name) {
        return actualSettings.setting(name)
            .or(() -> defaultSettings.setting(name));
    }
}
