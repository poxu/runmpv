package com.evilcorp.settings;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextFileSettingsTest {
    @Test
    public void streamConstructor() {
        final String rawSettings = ""
                + "waitSeconds=10\n"
                + "illegalSetting\n"
                +  "halfDefinedSetting= \n"
                +  "=\n"
                ;
        final TextFileSettings settings = new TextFileSettings(new ByteArrayInputStream(rawSettings.getBytes()));
        assertEquals("10", settings.setting("waitSeconds").orElseThrow());
        assertTrue(settings.setting("Seconds").isEmpty());
        assertTrue(settings.setting("illegalSefinedSetting").isEmpty());
        assertTrue(settings.setting("halfDefinedSetting").isEmpty());
    }

}