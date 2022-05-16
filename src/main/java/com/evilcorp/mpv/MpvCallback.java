package com.evilcorp.mpv;

public interface MpvCallback {
    void execute(String response, MpvEvents events, MpvMessageQueue server);
}
