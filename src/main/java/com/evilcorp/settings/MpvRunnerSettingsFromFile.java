package com.evilcorp.settings;

import com.evilcorp.fs.FsPaths;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MpvRunnerSettingsFromFile implements MpvRunnerProperties {
    private SoftSettings settings;
    private final FsPaths fsPaths;
    private boolean propertiesLoaded;

    public MpvRunnerSettingsFromFile(
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

    public MpvRunnerSettingsFromFile(
            InputStream inStream,
            FsPaths fsPaths
    ) {
        this.fsPaths = fsPaths;
        if (inStream == null) {
            settings = null;
            propertiesLoaded = false;
            return;
        }
        try {
            settings = new TextFileSettings(inStream);
//            settings.load(inStream);
            inStream.close();
            propertiesLoaded = true;
        } catch (IOException e) {
            settings = null;
            propertiesLoaded = false;
        }
    }

    @Override
    public Integer waitSeconds() {
        if (!propertiesLoaded) {
            return null;
        }
        final String waitSeconds = settings.setting("waitSeconds");
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
        final String mpvHomeDir = settings.setting("mpvHomeDir");
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
        return settings.setting("pipeName");
    }

    @Override
    public String mpvLogFile() {
        if (!propertiesLoaded) {
            return null;
        }
        final String mpvLogFile = settings.setting("mpvLogFile");
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
        final String runnerLogFile = settings.setting("runnerLogFile");
        if (runnerLogFile == null) {
            return null;
        }
        return fsPaths.resolve(runnerLogFile).path()
                .toString();
    }
}
