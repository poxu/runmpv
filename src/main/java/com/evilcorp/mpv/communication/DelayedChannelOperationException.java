package com.evilcorp.mpv.communication;

public class DelayedChannelOperationException extends RuntimeException {
    public DelayedChannelOperationException(String message) {
        super(message);
    }

    public DelayedChannelOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
