package com.evilcorp.mpv;

import java.math.BigDecimal;

public class ServerPauseResponse {
    private final String body;

    public ServerPauseResponse(String body) {
        this.body = body;
    }

    public boolean pause() {
        return body.contains("true");
    }

    public BigDecimal pos() {
        final int start = body.lastIndexOf(" ");
        return new BigDecimal(body.substring(start + 1));
    }

    public boolean valid() {
        return body.contains("pause");
    }
}
