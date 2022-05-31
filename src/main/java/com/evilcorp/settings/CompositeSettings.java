package com.evilcorp.settings;

import java.util.Optional;

/**
 * Settings, which compose actual settings and
 * default settings. Values are taken from actual settings
 * first and if value is not found, default settings
 * are searched for it.
 */
public class CompositeSettings implements SoftSettings {
    private final SoftSettings[] settingVariants;

    public CompositeSettings(
        SoftSettings... settingVariants
    ) {
        this.settingVariants = settingVariants;
    }

    @Override
    public Optional<String> setting(String name) {
        for (SoftSettings settings : settingVariants) {
            final Optional<String> setting = settings.setting(name);
            if (setting.isPresent()) {
                return setting;
            }
        }
        return Optional.empty();
    }
}
