package com.evilcorp;

import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.ManualFsFile;
import com.evilcorp.settings.MpvRunnerProperties;
import com.evilcorp.settings.MpvRunnerSettingsFromFile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MpvRunnerSettingsFromFileTest {
    @Test
    public void fileNotFound() {
        final MpvRunnerProperties mpvRunnerPropertiesFromFile = new MpvRunnerSettingsFromFile("non existent file",
                new LocalFsPaths(
                        new ManualFsFile(Path.of("first")),
                        new ManualFsFile(Path.of("second")),
                        new ManualFsFile(Path.of("third"))
                )
        );
        assertNull(mpvRunnerPropertiesFromFile.pipeName());
        assertNull(mpvRunnerPropertiesFromFile.mpvHomeDir());
        assertNull(mpvRunnerPropertiesFromFile.mpvLogFile());
        assertEquals("second", mpvRunnerPropertiesFromFile.executableDir());
        assertNull(mpvRunnerPropertiesFromFile.runnerLogFile());
        assertNull(mpvRunnerPropertiesFromFile.waitSeconds());
    }

    @Test
    public void propertyNotFound() {
        final MpvRunnerProperties mpvRunnerPropertiesFromFile = new MpvRunnerSettingsFromFile(
                new ByteArrayInputStream("waitSeconds=10".getBytes()),
                new LocalFsPaths(
                        new ManualFsFile(Path.of("first")),
                        new ManualFsFile(Path.of("second")),
                        new ManualFsFile(Path.of("third"))
                )
        );
        assertNull(mpvRunnerPropertiesFromFile.pipeName());
        assertNull(mpvRunnerPropertiesFromFile.mpvHomeDir());
        assertNull(mpvRunnerPropertiesFromFile.mpvLogFile());
        assertEquals("second", mpvRunnerPropertiesFromFile.executableDir());
        assertNull(mpvRunnerPropertiesFromFile.runnerLogFile());
        assertEquals(10, mpvRunnerPropertiesFromFile.waitSeconds());
    }
}
