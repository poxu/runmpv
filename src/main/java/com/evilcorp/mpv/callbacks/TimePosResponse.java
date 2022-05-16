package com.evilcorp.mpv.callbacks;

import java.math.BigDecimal;

public class TimePosResponse {
    private final String body;

    public TimePosResponse(String body) {
        this.body = body;
    }

    public BigDecimal pos() {
        final int startIdx = body.indexOf(":");
        final int endIdx = body.indexOf(",");
        final String pos = body.substring(startIdx + 1, endIdx);
        return new BigDecimal(pos);
    }

    public boolean available() {
        return !body.contains("property unavailable");
    }
}
