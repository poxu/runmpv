package com.evilcorp.mpv;

import java.util.Optional;

public interface MpvMessageQueue {
    void send(String msg);

    Optional<String> nextMessage();

    void close();
}
