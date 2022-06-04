package com.evilcorp.settings;

import java.util.HashMap;
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

    public ManualSettings(String... settings) {
        if (settings.length % 2 != 0) {
            throw new RuntimeException("You should pass pairs to ManualSettings");
        }
        this.settings = new HashMap<>();
        for (int i = 0; i < settings.length; i += 2) {
            this.settings.put(settings[i], settings[i + 1]);
        }
    }

    @Override
    public Optional<String> setting(String name) {
        return Optional.ofNullable(settings.get(name));
    }
}
