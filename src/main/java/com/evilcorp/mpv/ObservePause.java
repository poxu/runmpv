package com.evilcorp.mpv;

import com.evilcorp.mpv.commands.GetProperty;

public class ObservePause implements MpvCallback {
    @Override
    public void execute(String response, MpvEvents events, MpvMessageQueue server) {
        ObserveBooleanPropertyResponse pause = new ObserveBooleanPropertyResponse(response);
        if (!pause.available()) {
            return;
        }
        events.execute(new GetProperty("time-pos"), (msg, __1, __2) -> {
            final TimePosResponse timePosResponse = new TimePosResponse(msg);
            if (!timePosResponse.available()) {
                return;
            }
            server.send(new ServerPauseCommand(pause.isTrue(), timePosResponse.pos()).content());
        });
    }
}
