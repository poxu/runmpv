package com.evilcorp.cmd;

public class ExecutableNotFoundException extends RuntimeException {
    public ExecutableNotFoundException(String message) {
        super(message);
    }

    public ExecutableNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
