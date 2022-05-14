package com.evilcorp.mpv;

public class WriteTimeoutExceededException extends RuntimeException {
    public WriteTimeoutExceededException(String message) {
        super(message);
    }

    public WriteTimeoutExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
