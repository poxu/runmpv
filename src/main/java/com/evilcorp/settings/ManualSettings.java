package com.evilcorp.settings;

import java.util.Map;
import java.util.Optional;

public class ManualSettings implements SoftSettings {
    private final Map<String, String> settings;

    public ManualSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    @Override
    public Optional<String> setting(String name) {
        return Optional.ofNullable(settings.get(name));
    }
}
