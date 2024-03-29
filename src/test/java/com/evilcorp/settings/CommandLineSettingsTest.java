package com.evilcorp.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandLineSettingsTest {
    private static final String[] RAW_SETTINGS = {
        "--waitSeconds=10",
        "--halfDefinedSetting="
    };
    private final CommandLineSettings settings =
        new CommandLineSettings(RAW_SETTINGS);

    @Test
    public void legalProperty() {
        assertEquals("10", settings.setting("waitSeconds").orElseThrow());
    }

    @Test
    public void notExistingProperty() {
        assertTrue(settings.setting("Seconds").isEmpty());
    }

    @Test
    public void existingIllegalProperty() {
        assertTrue(settings.setting("halfDefinedSetting").isEmpty());
    }
}
