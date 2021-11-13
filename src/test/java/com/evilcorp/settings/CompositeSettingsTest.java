package com.evilcorp.settings;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositeSettingsTest {

    private CompositeSettings settings = new CompositeSettings(
        new ManualSettings(Map.of(
            "uniqueSetting", "uniqueValue",
            "actualSetting", "actualValue"
        )),
        new ManualSettings(Map.of(
            "actualSetting", "defaultValue",
            "defaultSetting", "defaultValue"
        ))
    );

    @Test
    public void nonExistentProperty() {
        assertTrue(settings.setting("non-existent-setting").isEmpty());
    }

    @Test
    public void uniqueNonDefault() {
        assertEquals("uniqueValue", settings.setting("uniqueSetting").orElseThrow());
    }

    @Test
    public void actualWithDefaultPresent() {
        assertEquals("actualValue", settings.setting("actualSetting").orElseThrow());
    }

    @Test
    public void defaultOnlyPresent() {
        assertEquals("defaultValue", settings.setting("defaultSetting").orElseThrow());
    }
}