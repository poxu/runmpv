package com.evilcorp.mpv;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Command to get property from mpv.
 *
 * Marks itself with randomly generated requestId, which then is used by
 * mpv to mark the message, containing property value.
 *
 */
public class GetProperty implements MpvCommand {
    private final String propertyName;
    private final int requestId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);

    /**
     * Creates {@link MpvCommand} to find mpv property
     * @param propertyName taken from mpv documentation.
     */
    public GetProperty(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public String content() {
        return "{\"command\": [\"get_property\",\"" + propertyName + "\"], \"request_id\": \"" + requestId + "\"  }\n";
    }

    /**
     * Randomly generated number which is supposed to uniquely identify
     * this particular instance of MpvCommand.
     *
     * Developer is supposed to use this requesId to find response to
     * this particular property request. So you probably shouldn't reuse
     * this GetProperty objects.
     */
    public int requestId() {
        return requestId;
    }
}
