package com.evilcorp;

import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.ManualFsFile;
import com.evilcorp.settings.MpvRunnerPropertiesFromFile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MpvRunnerPropertiesFromFileTest {
    @Test
    public void fileNotFound() {
        final MpvRunnerPropertiesFromFile mpvRunnerPropertiesFromFile = new MpvRunnerPropertiesFromFile("non existent file",
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
        final MpvRunnerPropertiesFromFile mpvRunnerPropertiesFromFile = new MpvRunnerPropertiesFromFile(
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
