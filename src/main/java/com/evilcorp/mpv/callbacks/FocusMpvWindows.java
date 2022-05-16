package com.evilcorp.mpv.callbacks;

import com.evilcorp.cmd.CommandLine;
import com.evilcorp.mpv.MpvCallback;
import com.evilcorp.mpv.MpvEvents;
import com.evilcorp.mpv.MpvMessageQueue;
import com.evilcorp.settings.RunMpvProperties;

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
 * Current solution uses wscript from here
 * <a href="https://stackoverflow.com/a/56122113">https://stackoverflow.com/a/56122113</a>
 * <p>
 * It would probably be more reliable to use PowerShell
 * <a href="https://stackoverflow.com/questions/42566799/how-to-bring-focus-to-window-by-process-name">https://stackoverflow.com/questions/42566799/how-to-bring-focus-to-window-by-process-name</a>
 * <a href="https://stackoverflow.com/a/58548853">https://stackoverflow.com/a/58548853</a>
 * but it doesn't work unless admin gave user an explicit permission
 */
public class FocusMpvWindows implements MpvCallback {
    Logger logger = Logger.getLogger(FocusMpvWindows.class.getName());
    private final CommandLine commandLine;
    private final boolean firstLaunch;
    private final RunMpvProperties config;

    public FocusMpvWindows(
        CommandLine commandLine,
        boolean firstLaunch,
        RunMpvProperties config
    ) {
        this.commandLine = commandLine;
        this.firstLaunch = firstLaunch;
        this.config = config;
    }

    @Override
    public void execute(String response, MpvEvents events, MpvMessageQueue serverQueue) {
        if (firstLaunch) {
            return;
        }
        final int startIdx = response.indexOf(":");
        final int endIdx = response.indexOf(",");
        final String pid = response.substring(startIdx + 1, endIdx).replaceAll("\"", "");
        logger.info("PID = " + pid);
        final List<String> focusArgs = List.of(
            "wscript",
            "/B",
            config.executableDir() + "/focus.vbs",
            pid
        );

        commandLine.runOrExecute(String.join(" ", focusArgs),
            (e) -> logger.log(Level.INFO, "Couldn't focus mpv window", e));
    }
}
