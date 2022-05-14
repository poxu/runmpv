package com.evilcorp.mpv;

public interface MpvCommunicationChannel {
    boolean isOpen();

    FixedTimeoutByteChannel channel();

    void attach();

    void detach();

    String name();
}
