package com.evilcorp.mpv;

import java.math.BigDecimal;

public class ServerPauseCommand {
    private final boolean pause;
    private final BigDecimal pos;

    public ServerPauseCommand(boolean pause, BigDecimal pos) {
        this.pause = pause;
        this.pos = pos;
    }

    public String content() {
        return "pause " + pause + " " + pos + "\n";
    }
}
