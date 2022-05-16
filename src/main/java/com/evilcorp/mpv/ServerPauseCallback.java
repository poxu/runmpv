package com.evilcorp.mpv;

import com.evilcorp.mpv.commands.SetProperty;

public class ServerPauseCallback implements MpvCallback {
    @Override
    public void execute(String response, MpvEvents events, MpvMessageQueue server) {
        final ServerPauseResponse resp = new ServerPauseResponse(response);
        if (!resp.valid()) {
            return;
        }
        if (resp.pause()) {
            events.execute(new SetProperty("pause", true));
            events.execute(new SetProperty("time-pos", resp.pos()));
        } else {
            events.execute(new SetProperty("time-pos", resp.pos()));
            events.execute(new SetProperty("pause", false));
        }
    }
}
