package com.evilcorp.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandLineSettingsBase64Test {
    private static final String[] RAW_SETTINGS = {
        "--halfDefinedSetting=",
        "--uncodedSetting=123",
        "LS13YWl0U2Vjb25kcz0xMA==",
        "LS1oYWxmRGVmaW5lZFNldHRpbmc9",
        "LS13aGl0ZXNwYWNlU2V0dGluZz0g",
        "IA==",
        ""
    };
    private final CommandLineSettingsBase64 settings =
        new CommandLineSettingsBase64(RAW_SETTINGS);

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

    @Test
    public void whitespaceOnlyProperty() {
        assertTrue(settings.setting("whitespaceSetting").isEmpty());
    }

    @Test
    public void uncodedProperty() {
        assertTrue(settings.setting("uncodedSetting").isEmpty());
    }
}
