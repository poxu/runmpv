package com.evilcorp.settings;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextFileSettingsTest {
    private static final String RAW_SETTINGS = """
        # comment
        # waitSeconds=15
        # waitSeconds=16
        waitSeconds=10
        illegalSetting
        halfDefinedSetting=\s
        =
        """;
    private final TextFileSettings settings = new TextFileSettings(new ByteArrayInputStream(RAW_SETTINGS.getBytes()));

    @Test
    public void legalProperty() {
        assertEquals("10", settings.setting("waitSeconds").orElseThrow());
    }

    @Test
    public void notExistingProperty() {
        assertTrue(settings.setting("Seconds").isEmpty());
        assertTrue(settings.setting("halfDefinedSetting").isEmpty());
    }

    @Test
    public void existingIllegalProperty() {
        assertTrue(settings.setting("halfDefinedSetting").isEmpty());
    }
}