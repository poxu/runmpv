package com.evilcorp.mpv;

import com.evilcorp.os.OperatingSystemFamily;
import com.evilcorp.settings.MpvRunnerProperties;

public class MvpInstanceProvider {
    private final MpvRunnerProperties properties;
    private final OperatingSystemFamily os;

    public MvpInstanceProvider(MpvRunnerProperties properties, OperatingSystemFamily os) {
        if (os == null) {
            throw new IllegalArgumentException("os argument can't be null");
        }
        this.properties = properties;
        this.os = os;
    }

    public MpvInstance mvpInstance() {
        return switch (os) {
            case WINDOWS -> new MpvInstanceWindows(properties);
            case LINUX -> new MpvInstanceLinux(properties);
            default -> throw new IllegalArgumentException("unexpected os for mpv - " + os);
        };
    }
}
