package com.evilcorp;

import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.ManualFsFile;
import com.evilcorp.settings.ManualMpvRunnerProperties;
import com.evilcorp.settings.MpvRunnerPropertiesFromFile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MpvRunnerPropertiesFromFileTest {
    @Test
    public void fileNotFound() {
        final MpvRunnerPropertiesFromFile mpvRunnerPropertiesFromFile = new MpvRunnerPropertiesFromFile("non existent file",
                new LocalFsPaths(
                        new ManualFsFile(Path.of("first")),
                        new ManualFsFile(Path.of("second")),
                        new ManualFsFile(Path.of("third"))
                ),
                new ManualMpvRunnerProperties(
                        (short) 5,
                        "mpvHomeDir",
                        "pipeName",
                        "mpvLogFile",
                        "executableDir",
                        "runnerLogFile"
                )
        );
        assertEquals("pipeName", mpvRunnerPropertiesFromFile.pipeName());
        assertEquals("mpvHomeDir", mpvRunnerPropertiesFromFile.mpvHomeDir());
        assertEquals("mpvLogFile", mpvRunnerPropertiesFromFile.mpvLogFile());
        assertEquals("executableDir", mpvRunnerPropertiesFromFile.executableDir());
        assertEquals("runnerLogFile", mpvRunnerPropertiesFromFile.runnerLogFile());
        assertEquals(5, mpvRunnerPropertiesFromFile.waitSeconds());
    }

    @Test
    public void propertyNotFound() {
        final MpvRunnerPropertiesFromFile mpvRunnerPropertiesFromFile = new MpvRunnerPropertiesFromFile(
                new ByteArrayInputStream("waitSeconds=10".getBytes()),
                new LocalFsPaths(
                        new ManualFsFile(Path.of("first")),
                        new ManualFsFile(Path.of("second")),
                        new ManualFsFile(Path.of("third"))
                ),
                new ManualMpvRunnerProperties(
                        (short) 5,
                        "mpvHomeDir",
                        "pipeName",
                        "mpvLogFile",
                        "executableDir",
                        "runnerLogFile"
                )
        );
        assertEquals("pipeName", mpvRunnerPropertiesFromFile.pipeName());
        assertEquals("mpvHomeDir", mpvRunnerPropertiesFromFile.mpvHomeDir());
        assertEquals("mpvLogFile", mpvRunnerPropertiesFromFile.mpvLogFile());
        assertEquals("second", mpvRunnerPropertiesFromFile.executableDir());
        assertEquals("runnerLogFile", mpvRunnerPropertiesFromFile.runnerLogFile());
        assertEquals(10, mpvRunnerPropertiesFromFile.waitSeconds());
    }
}
