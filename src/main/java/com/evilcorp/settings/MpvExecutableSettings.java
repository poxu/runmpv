package com.evilcorp.settings;

import com.evilcorp.os.OperatingSystem;

import java.util.Optional;

public class MpvExecutableSettings implements SoftSettings {
    private OperatingSystem os;
    private String windowsPath;
    private String linuxPath;

    public MpvExecutableSettings(
        OperatingSystem os,
        String windowsPath,
        String linuxPath
    ) {
        this.os = os;
        this.windowsPath = windowsPath;
        this.linuxPath = linuxPath;
    }

    @Override
    public Optional<String> setting(String name) {
        if (!"mpvHomeDir".equals(name)) {
            return Optional.empty();
        }
        return switch (os.operatingSystemFamily()) {
            case WINDOWS -> Optional.of(windowsPath);
            case LINUX -> Optional.of(linuxPath);
            default -> throw new IllegalArgumentException("unexpected os for mpv - " + os);
        };
    }
}
