package com.evilcorp.settings;


import com.evilcorp.os.OperatingSystem;
import com.evilcorp.os.OperatingSystemFamily;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MpvExecutableSettingsTest {

    private OperatingSystem os = () -> OperatingSystemFamily.WINDOWS;
    private SoftSettings settings = new MpvExecutableSettings(os, "%r/..", "/usr/bin/mpv");

    @Test
    public void emptySetting() {
        assertTrue(settings.setting("non-existent-setting").isEmpty());
    }

    @Test
    public void windowsSetting() {
        assertEquals("%r/..", settings.setting("mpvHomeDir").orElseThrow());
    }

    @Test
    public void linuxSetting() {
        OperatingSystem os = () -> OperatingSystemFamily.LINUX;
        SoftSettings settings = new MpvExecutableSettings(os, "%r/..", "");
        assertEquals("", settings.setting("mpvHomeDir").orElseThrow());
    }
}