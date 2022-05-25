package com.evilcorp.mpv.callbacks;

import com.evilcorp.cmd.CommandLine;
import com.evilcorp.cmd.ExecutableNotFoundException;
import com.evilcorp.mpv.MpvCallback;
import com.evilcorp.mpv.MpvEvents;
import com.evilcorp.mpv.MpvMessageQueue;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Focus mpv window. It is not a command, because
 * mpv doesn't have api to focus itself. It is supposed
 * that you use window manager to do that.
 * So, you either should have a different implementation
 * of MpvInstance per window manager, or inject and implementation
 * of focus via constructor, or implement focusing as a callback.
 *
 */
public class FocusMpvLinux implements MpvCallback {
    Logger logger = Logger.getLogger(FocusMpvLinux.class.getName());
    private final CommandLine commandLine;

    public FocusMpvLinux(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public void execute(String response, MpvEvents events, MpvMessageQueue serverQueue) {
        final int startIdx = response.indexOf(":");
        final int endIdx = response.indexOf(",");
        final String pid = response.substring(startIdx + 1, endIdx).replaceAll("\"", "");
        logger.info("PID = " + pid);
        final String wid;
        try {
            wid = commandLine.singleResultOrThrow("xdotool search --pid " + pid);
        } catch (ExecutableNotFoundException e) {
            logger.log(Level.SEVERE, "xdotool is probably not installed. " +
                " runmpv needs xdotool to focus mpv window.", e);
            return;
        }

        final List<String> focusArgs = List.of(
            "xdotool",
            "windowraise",
            wid
        );
        commandLine.runOrThrow(focusArgs);
    }
}
