package com.evilcorp.mpv.callbacks;

import com.evilcorp.cmd.CommandLine;
import com.evilcorp.cmd.Retry;
import com.evilcorp.mpv.MpvCallback;
import com.evilcorp.mpv.MpvEvents;
import com.evilcorp.mpv.MpvMessageQueue;

import java.util.List;
import java.util.Optional;
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
        Retry<String> findWindowId = new Retry<>(4, () -> {
            try {
                return Optional.of(commandLine.singleResultOrThrow("xdotool search --pid " + pid));
            } catch (RuntimeException e) {
                logger.log(Level.INFO, "Couldn't find mpv pid", e);
                return Optional.empty();
            }
        });

        Optional<String> wid = findWindowId.get();
        if (wid.isEmpty()) {
            logger.info("Couldn't wait until mpv starts");
            return;
        }
        final List<String> focusArgs = List.of(
            "xdotool",
            "windowraise",
            wid.orElseThrow()
        );
        commandLine.runOrThrow(focusArgs);
    }
}
