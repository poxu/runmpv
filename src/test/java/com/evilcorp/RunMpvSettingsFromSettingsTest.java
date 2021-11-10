package com.evilcorp;

import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.ManualFsFile;
import com.evilcorp.settings.ManualSettings;
import com.evilcorp.settings.MpvRunnerProperties;
import com.evilcorp.settings.MpvRunnerPropertiesFromSettings;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunMpvSettingsFromSettingsTest {

    private MpvRunnerProperties properties = new MpvRunnerPropertiesFromSettings(
            new ManualSettings(
                    Map.of(
                            "waitSeconds", "10",
                            "mpvHomeDir", "%h/soft/mpv",
                            "pipeName", "myPipe",
                            "mpvLogFile", "%r/debug.log",
                            "runnerLogFile", "%v/runner-debug.log",
                            "executableDir", "execDir",
                            "focusAfterOpen", "true"
                    )
            ),
            new LocalFsPaths(
                    new ManualFsFile(Path.of("home")),
                    new ManualFsFile(Path.of("runmpv")),
                    new ManualFsFile(Path.of("video"))
            )
    );

    @Test
    public void waitSeconds() {
        assertEquals(10, properties.waitSeconds());
    }

    @Test
    public void mpvHomeDir() {
        assertEquals("home/soft/mpv", properties.mpvHomeDir()
                .replace('\\','/'));
    }
    @Test
    public void pipeName() {
        assertEquals("myPipe", properties.pipeName()
                .replace('\\','/'));
    }

    @Test
    public void mpvLogFile() {
        assertEquals("runmpv/debug.log", properties.mpvLogFile()
                .replace('\\','/'));
    }

    @Test
    public void runnerLogFile() {
        assertEquals("video/runner-debug.log", properties.runnerLogFile()
                .replace('\\','/'));
    }

    @Test
    public void executableDir() {
        assertEquals("execDir", properties.executableDir()
                .replace('\\','/'));
    }

    @Test
    public void focusAfterOpen() {
        assertTrue(properties.focusAfterOpen());
    }
}
