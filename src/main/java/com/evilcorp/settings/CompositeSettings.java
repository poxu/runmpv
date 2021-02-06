package com.evilcorp.settings;

import java.util.Optional;

/**
 * Settings, which compose actual settigns and
 * default settings. Values are taken from actual settings
 * first and if values is not found, default settings
 * are searched.
 */
public class CompositeSettings implements SoftSettings {
    private final SoftSettings actualSettings;
    private final SoftSettings defautlSettings;

    public CompositeSettings(
            SoftSettings actualSettings,
            SoftSettings defautlSettings
    ) {
        this.actualSettings = actualSettings;
        this.defautlSettings = defautlSettings;
    }

    @Override
    public Optional<String> setting(String name) {
        return actualSettings.setting(name)
                .or(() -> defautlSettings.setting(name));
    }
}
