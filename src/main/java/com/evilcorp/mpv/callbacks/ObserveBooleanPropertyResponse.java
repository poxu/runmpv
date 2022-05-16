package com.evilcorp.mpv.callbacks;

public class ObserveBooleanPropertyResponse {
    private final String body;

    public ObserveBooleanPropertyResponse(String body) {
        this.body = body;
    }

    public boolean available() {
        return body.contains("data");
    }

    public boolean isTrue() {
        return body.contains("true");
    }
}
