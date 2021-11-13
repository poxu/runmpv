package com.evilcorp.mpv;

/**
 * Represents a command sent to mpv.
 * Currently, the only thing it's supposed
 * to do is provide a string with content.
 * <p>
 * Commands are sent to mpv in string form
 * via sockets or via pipes. Format used for
 * pipes is too suited for sockets, so probably
 * two classes will exist for one command.
 */
public interface MpvCommand {
    /**
     * String, which is going to be sent to mpv via socket or pipe
     */
    String content();
}
