package com.evilcorp.mpv;

import com.evilcorp.os.OperatingSystemFamily;
import com.evilcorp.settings.RunMpvProperties;

public class MvpInstanceProvider {
    private final RunMpvProperties properties;
    private final OperatingSystemFamily os;

    public MvpInstanceProvider(RunMpvProperties properties, OperatingSystemFamily os) {
        if (os == null) {
            throw new IllegalArgumentException("os argument can't be null");
        }
        this.properties = properties;
        this.os = os;
    }

    public MpvInstance mvpInstance() {
        return switch (os) {
            case WINDOWS -> new MpvInstanceWindows(properties,
                new WindowsMpvCommunicationChannel(properties));
            case LINUX -> new MpvInstanceLinux(properties,
                new LinuxMpvCommunicationChannel(properties));
            default -> throw new IllegalArgumentException("unexpected os for mpv - " + os);
        };
    }
}
