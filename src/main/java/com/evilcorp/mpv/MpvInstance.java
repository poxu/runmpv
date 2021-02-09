package com.evilcorp.mpv;

public interface MpvInstance {
    void execute(MpvCommand command);
    void focus();
}
