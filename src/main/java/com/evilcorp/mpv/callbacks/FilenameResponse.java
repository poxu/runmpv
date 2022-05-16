package com.evilcorp.mpv.callbacks;

public class FilenameResponse {
    private final String body;

    public FilenameResponse(String body) {
        this.body = body;
    }

    public String filename() {
        final int startIdx = body.indexOf(":");
        final int endIdx = body.indexOf(",");
        final String name = body.substring(startIdx + 2, endIdx - 1);
        return name;
    }

    public boolean available() {
        return !body.contains("property unavailable");
    }
}
