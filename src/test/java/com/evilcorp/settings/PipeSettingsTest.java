package com.evilcorp.settings;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PipeSettingsTest {
    @Test
    public void singlePipe() {
        SoftSettings pipeSettings = new PipeSettings(
            new ManualSettings(Map.of("pipeName", "pipe-per-directory")),
            new ManualSettings(
                Map.of(
                    "openMode", "single-instance",
                    "pipeName", "single-pipe"
                )
            )
        );
        assertEquals("single-pipe", pipeSettings.setting("pipeName").orElseThrow());
    }

    @Test
    public void pipePerDirectory() {
        SoftSettings pipeSettings = new PipeSettings(
            new ManualSettings(Map.of("pipeName", "pipe-per-directory")),
            new ManualSettings(
                Map.of(
                    "openMode", "instance-per-directory",
                    "pipeName", "single-pipe"
                )
            )
        );
        assertEquals("pipe-per-directory", pipeSettings.setting("pipeName").orElseThrow());
    }
}
