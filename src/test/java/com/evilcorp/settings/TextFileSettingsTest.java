package com.evilcorp.settings;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextFileSettingsTest {

    private final String RAW_SETTINGS = ""
            + "waitSeconds=10\n"
            + "illegalSetting\n"
            +  "halfDefinedSetting= \n"
            +  "=\n";
    private TextFileSettings settings = new TextFileSettings(new ByteArrayInputStream(RAW_SETTINGS.getBytes()));

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