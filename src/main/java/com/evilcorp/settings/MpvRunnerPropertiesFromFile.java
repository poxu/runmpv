package com.evilcorp.settings;

import com.evilcorp.fs.FsPaths;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MpvRunnerPropertiesFromFile implements MpvRunnerProperties {
    private Properties properties;
    private final FsPaths fsPaths;
    private boolean propertiesLoaded;

    public MpvRunnerPropertiesFromFile(
            String propertyFileName,
            FsPaths fsPaths
    ) {
        this(getInStream(propertyFileName, fsPaths), fsPaths);
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
            FsPaths fsPaths
    ) {
        this.fsPaths = fsPaths;
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
    public Integer waitSeconds() {
        if (!propertiesLoaded) {
            return null;
        }
        final String waitSeconds = properties.getProperty("waitSeconds");
        if (waitSeconds == null) {
            return null;
        }
        return Integer.parseInt(waitSeconds);
    }

    @Override
    public String mpvHomeDir() {
        if (!propertiesLoaded) {
            return null;
        }
        final String mpvHomeDir = properties.getProperty("mpvHomeDir");
        if (mpvHomeDir == null) {
            return null;
        }
        return fsPaths.resolve(mpvHomeDir).path()
                .toString();
    }

    @Override
    public String pipeName() {
        if (!propertiesLoaded) {
            return null;
        }
        return properties.getProperty("pipeName");
    }

    @Override
    public String mpvLogFile() {
        if (!propertiesLoaded) {
            return null;
        }
        final String mpvLogFile = properties.getProperty("mpvLogFile");
        if (mpvLogFile == null) {
            return null;
        }
        return fsPaths.resolve(mpvLogFile).path()
                .toString();
    }

    @Override
    public String executableDir() {
        return fsPaths.resolve("%r").path()
                .toString();
    }

    @Override
    public String runnerLogFile() {
        if (!propertiesLoaded) {
            return null;
        }
        final String runnerLogFile = properties.getProperty("runnerLogFile");
        if (runnerLogFile == null) {
            return null;
        }
        return fsPaths.resolve(runnerLogFile).path()
                .toString();
    }
}
