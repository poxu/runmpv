package com.evilcorp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MpvRunnerPropertiesFromFile implements MpvRunnerProperties {
    private final Properties properties;
    private final FsPaths fsPaths;

    public MpvRunnerPropertiesFromFile(
            String propertyFileName,
            FsPaths fsPaths,
            MpvRunnerProperties defaultPropertyValues
    ) {
        this.fsPaths = fsPaths;
        try {
            final String fullFileName = fsPaths.resolve("%r/" + propertyFileName).path()
                    .toString();
            final FileInputStream inStream = new FileInputStream(fullFileName);
            properties = new Properties();
            properties.load(inStream);
            inStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short waitSeconds() {
        return Short.parseShort(properties.getProperty("waitSeconds"));
    }

    @Override
    public String mpvHomeDir() {
        return fsPaths.resolve(properties.getProperty("mpvHomeDir")).path()
                .toString();
    }

    @Override
    public String pipeName() {
        return properties.getProperty("pipeName");
    }

    @Override
    public String mpvLogFile() {
        return fsPaths.resolve(properties.getProperty("mpvLogFile")) .path()
                .toString();
    }

    @Override
    public String executableDir() {
        return fsPaths.resolve("%r").path()
                .toString();
    }

    @Override
    public String runnerLogFile() {
        return fsPaths.resolve(properties.getProperty("runnerLogFile")).path()
                .toString();
    }
}
