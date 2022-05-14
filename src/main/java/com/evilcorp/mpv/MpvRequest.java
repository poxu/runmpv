package com.evilcorp.mpv;

public interface MpvRequest extends MpvCommand {
    /**
     * Randomly generated number which is supposed to uniquely identify
     * this particular instance of MpvRequest.
     *
     * Developer is supposed to use this requestId to find response to
     * this particular request. So you probably shouldn't reuse
     * MpvRequest objects.
     */
    int requestId();
}
