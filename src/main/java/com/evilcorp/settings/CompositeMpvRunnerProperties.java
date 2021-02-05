package com.evilcorp.settings;

public class CompositeMpvRunnerProperties implements MpvRunnerProperties {
    private final MpvRunnerProperties actualProperties;
    private final MpvRunnerProperties defaultProperties;

    public CompositeMpvRunnerProperties(
            MpvRunnerProperties actualProperties,
            MpvRunnerProperties defaultProperties
    ) {

        this.actualProperties = actualProperties;
        this.defaultProperties = defaultProperties;
    }

    @Override
    public Integer waitSeconds() {
        final Integer waitSeconds = actualProperties.waitSeconds();
        if (waitSeconds == null) {
            return defaultProperties.waitSeconds();
        }
        return waitSeconds;
    }

    @Override
    public String mpvHomeDir() {
        final String mpvHomeDir = actualProperties.mpvHomeDir();
        if (mpvHomeDir == null) {
            return defaultProperties.mpvHomeDir();
        }
        return mpvHomeDir;
    }

    @Override
    public String pipeName() {
        final String pipeName = actualProperties.pipeName();
        if (pipeName == null) {
            return defaultProperties.pipeName();
        }
        return pipeName;
    }

    @Override
    public String mpvLogFile() {
        final String mpvLogFile = actualProperties.mpvLogFile();
        if (mpvLogFile == null) {
            return defaultProperties.mpvLogFile();
        }
        return mpvLogFile;
    }

    @Override
    public String executableDir() {
        return actualProperties.executableDir();
    }

    @Override
    public String runnerLogFile() {
        final String runnerLogFile = actualProperties.runnerLogFile();
        if (runnerLogFile == null) {
            return defaultProperties.runnerLogFile();
        }
        return runnerLogFile;
    }
}
