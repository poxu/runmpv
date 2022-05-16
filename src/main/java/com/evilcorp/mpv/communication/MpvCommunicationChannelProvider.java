package com.evilcorp.mpv.communication;

import com.evilcorp.mpv.MpvCommunicationChannel;
import com.evilcorp.os.OperatingSystemFamily;
import com.evilcorp.settings.RunMpvProperties;

public class MpvCommunicationChannelProvider {
    private final RunMpvProperties properties;
    private final OperatingSystemFamily os;

    public MpvCommunicationChannelProvider(RunMpvProperties properties, OperatingSystemFamily os) {
        if (os == null) {
            throw new IllegalArgumentException("os argument can't be null");
        }
        this.properties = properties;
        this.os = os;
    }

    public MpvCommunicationChannel channel() {
        return switch (os) {
            case WINDOWS -> new WindowsMpvCommunicationChannel(properties);
            case LINUX -> new LinuxMpvCommunicationChannel(properties);
            default -> throw new IllegalArgumentException("unexpected os for mpv - " + os);
        };
    }
}
