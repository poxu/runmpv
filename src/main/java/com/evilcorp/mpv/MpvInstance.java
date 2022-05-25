package com.evilcorp.mpv;

/**
 * An interface for controlling an instance of mpv.
 * Implementation should control one instance of mpv.
 */
public interface MpvInstance {
    MpvCallback focusCallback();

    /**
     * This method is used to check if mpv existed prior ot runmpv was called.
     *
     * @return true if mpv executable was only started, after this object was
     * created.
     */
    boolean firstLaunch();
}
