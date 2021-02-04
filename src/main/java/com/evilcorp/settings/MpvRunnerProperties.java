package com.evilcorp.settings;

public interface MpvRunnerProperties {
    Integer waitSeconds();

    String mpvHomeDir();

    String pipeName();

    String mpvLogFile();

    String executableDir();

    String runnerLogFile();
}
