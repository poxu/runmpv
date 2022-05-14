package com.evilcorp.mpv;

/**
 * An interface for controlling an instance of mpv.
 * Implementation should control one instance of mpv.
 */
public interface MpvInstance {
    /**
     * Send a command to mpv instance
     *
     * @param command action mpv should execute
     */
    void execute(MpvCommand command);

    /**
     * Send a command to mpv instance
     * executing callback after command is finished
     *
     * @param command action mpv should execute
     */
    void execute(MpvRequest command, MpvCallback callback);

    MpvCallback focusCallback();

    void receiveMessages();

    boolean hasPendingRequests();
}
