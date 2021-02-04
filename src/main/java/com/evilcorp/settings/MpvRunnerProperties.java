package com.evilcorp.settings;

public interface MpvRunnerProperties {
    short waitSeconds();

    String mpvHomeDir();

    String pipeName();

    String mpvLogFile();

    String executableDir();

    String runnerLogFile();
}
