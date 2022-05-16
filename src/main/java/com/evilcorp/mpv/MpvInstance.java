package com.evilcorp.mpv;

/**
 * An interface for controlling an instance of mpv.
 * Implementation should control one instance of mpv.
 */
public interface MpvInstance {
    MpvCallback focusCallback();
}
