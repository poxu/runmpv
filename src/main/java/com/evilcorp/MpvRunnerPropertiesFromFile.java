package com.evilcorp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MpvRunnerPropertiesFromFile implements MpvRunnerProperties {
    private Properties properties;
    private final FsPaths fsPaths;
    private final MpvRunnerProperties defaultPropertyValues;
    private boolean propertiesLoaded;

    public MpvRunnerPropertiesFromFile(
            String propertyFileName,
            FsPaths fsPaths,
            MpvRunnerProperties defaultPropertyValues
    ) {
        this(getInStream(propertyFileName, fsPaths), fsPaths, defaultPropertyValues);
    }

    private static InputStream getInStream(String propertyFileName, FsPaths fsPaths) {
        try {
            return new FileInputStream(fsPaths.resolve("%r/" + propertyFileName).path()
                    .toString());
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public MpvRunnerPropertiesFromFile(
            InputStream inStream,
            FsPaths fsPaths,
            MpvRunnerProperties defaultPropertyValues
    ) {
        this.fsPaths = fsPaths;
        this.defaultPropertyValues = defaultPropertyValues;
        if (inStream == null) {
            properties = null;
            propertiesLoaded = false;
            return;
        }
        try {
            properties = new Properties();
            properties.load(inStream);
            inStream.close();
            propertiesLoaded = true;
        } catch (IOException e) {
            properties = null;
            propertiesLoaded = false;
        }
    }

    @Override
    public short waitSeconds() {
        if (!propertiesLoaded) {
            return defaultPropertyValues.waitSeconds();
        }
        final String waitSeconds = properties.getProperty("waitSeconds");
        if (waitSeconds == null) {
            return defaultPropertyValues.waitSeconds();
        }
        return Short.parseShort(waitSeconds);
    }

    @Override
    public String mpvHomeDir() {
        if (!propertiesLoaded) {
            return defaultPropertyValues.mpvHomeDir();
        }
        final String mpvHomeDir = properties.getProperty("mpvHomeDir");
        if (mpvHomeDir == null) {
            return defaultPropertyValues.mpvHomeDir();
        }
        return fsPaths.resolve(mpvHomeDir).path()
                .toString();
    }

    @Override
    public String pipeName() {
        if (!propertiesLoaded) {
            return defaultPropertyValues.pipeName();
        }
        final String pipeName = properties.getProperty("pipeName");
        if (pipeName == null) {
            return defaultPropertyValues.pipeName();
        }
        return pipeName;
    }

    @Override
    public String mpvLogFile() {
        if (!propertiesLoaded) {
            return defaultPropertyValues.mpvLogFile();
        }
        final String mpvLogFile = properties.getProperty("mpvLogFile");
        if (mpvLogFile == null) {
            return defaultPropertyValues.mpvLogFile();
        }
        return fsPaths.resolve(mpvLogFile).path()
                .toString();
    }

    @Override
    public String executableDir() {
        if (!propertiesLoaded) {
            return defaultPropertyValues.executableDir();
        }
        return fsPaths.resolve("%r").path()
                .toString();
    }

    @Override
    public String runnerLogFile() {
        if (!propertiesLoaded) {
            return defaultPropertyValues.runnerLogFile();
        }
        final String runnerLogFile = properties.getProperty("runnerLogFile");
        if (runnerLogFile == null) {
            return defaultPropertyValues.runnerLogFile();
        }
        return fsPaths.resolve(runnerLogFile).path()
                .toString();
    }
}
