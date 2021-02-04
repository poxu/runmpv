package com.evilcorp;

import com.evilcorp.settings.CompositeMpvRunnerProperties;
import com.evilcorp.settings.ManualMpvRunnerProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CompositeMpvRunnerPropertiesTest {
    @Test
    public void defaultsAreFetchedWhenNeeded() {
        final CompositeMpvRunnerProperties settings = new CompositeMpvRunnerProperties(
                new ManualMpvRunnerProperties(
                        5,
                        "actualMpvHomeDir",
                        null,
                        "actualMpvLogFile",
                        null,
                        "actualRunnerLogFile"
                ),
                new ManualMpvRunnerProperties(
                        5,
                        "defaultMpvHomeDir",
                        "defaultPipeName",
                        "defaultMpvLogFile",
                        "defaultExecutableDir",
                        "defaultRunnerLogFile"
                )
        );
        assertEquals("actualMpvHomeDir", settings.mpvHomeDir());
        assertEquals(5, settings.waitSeconds());
        assertEquals("defaultPipeName", settings.pipeName());
        assertEquals("actualMpvLogFile", settings.mpvLogFile());
        assertNull(settings.executableDir());
        assertEquals("actualRunnerLogFile", settings.runnerLogFile());
    }
}
