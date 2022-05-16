package com.evilcorp.mpv;

import com.evilcorp.mpv.communication.FixedTimeoutByteChannel;

public interface MpvCommunicationChannel {
    boolean isOpen();

    FixedTimeoutByteChannel channel();

    void attach();

    void detach();

    String name();
}
