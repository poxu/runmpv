package com.evilcorp.settings;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals("10", settings.setting("waitSeconds"));
        assertNull(settings.setting("Seconds"));
        assertNull(settings.setting("illegalSefinedSetting"));
        assertNull(settings.setting("halfDefinedSetting"));
    }

}