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
     * Focus mpv window. It is not a command, because
     * mpv doesn't have api to focus itself. It is supposed
     * that you use window manager to do that.
     * So, you either should have a different implementation
     * of MpvInstance per window manager, or inject and implementation
     * of focus via constructor.
     */
    void focus();
}
