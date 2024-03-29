package com.evilcorp.mpv;

import com.evilcorp.os.OperatingSystemFamily;
import com.evilcorp.settings.RunMpvProperties;

public class MvpInstanceProvider {
    private final RunMpvProperties properties;
    private final OperatingSystemFamily os;
    private final MpvCommunicationChannel channel;

    public MvpInstanceProvider(
        RunMpvProperties properties,
        OperatingSystemFamily os,
        MpvCommunicationChannel channel
    ) {
        this.channel = channel;
        if (os == null) {
            throw new IllegalArgumentException("os argument can't be null");
        }
        this.properties = properties;
        this.os = os;
    }

    public MpvInstance mvpInstance() {
        return switch (os) {
            case WINDOWS -> new MpvInstanceWindows(properties,
                channel);
            case LINUX -> new MpvInstanceLinux(properties,
                channel);
            default -> throw new IllegalArgumentException("unexpected os for mpv - " + os);
        };
    }
}
