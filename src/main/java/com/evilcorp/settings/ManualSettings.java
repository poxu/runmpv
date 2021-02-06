package com.evilcorp.settings;

import java.util.Map;
import java.util.Optional;

/**
 * Settings, filled manually.
 */
public class ManualSettings implements SoftSettings {
    private final Map<String, String> settings;

    /**
     * @param settings Map, containing name -> value pairs
     *                 for settings. Used as is internally.
     */
    public ManualSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    @Override
    public Optional<String> setting(String name) {
        return Optional.ofNullable(settings.get(name));
    }
}
