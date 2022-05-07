package com.evilcorp.mpv;

import com.evilcorp.cmd.CommandLine;
import com.evilcorp.cmd.StandardCommandLine;
import com.evilcorp.settings.RunMpvProperties;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.evilcorp.util.Shortcuts.sleep;

public class MpvInstanceWindows implements MpvInstance {
    public static final String WINDOWS_PIPE_PREFIX = "\\\\.\\pipe\\";
    private final Logger logger;
    private final RunMpvProperties config;
    private final MpvMessageQueue queue;
    private final CommandLine commandLine;
    private final boolean firstLaunch;

    public MpvInstanceWindows(RunMpvProperties config) {
        this.config = config;
        logger = Logger.getLogger(MpvInstanceWindows.class.getName());
        this.commandLine = new StandardCommandLine(config.executableDir(), Collections.emptyMap());

        boolean firstLaunch;
        try {
            ByteChannel channel = FileChannel.open(Path.of(WINDOWS_PIPE_PREFIX).resolve(config.pipeName()), StandardOpenOption.READ, StandardOpenOption.WRITE);
            channel.close();
            firstLaunch = false;
        } catch (IOException e) {
            firstLaunch = true;
        }
        this.firstLaunch = firstLaunch;

        if (firstLaunch) {
            List<String> arguments = new ArrayList<>();

            // Using absolute path to specify executable.
            // PATH variable is, therefore, ignored.
            final String mpvExecutable = config.mpvHomeDir() + "/mpv.exe";
            arguments.add(mpvExecutable);

            // Argument is needed to make mpv show ui
            // if mpv.exe is launched with output stream redirected
            // somewhere else, then it just prints help to terminal.
            // One workaround is to pass filename argument.
            // Then ui is shown.
            // Other workaround, which is used here, is to provide
            // this argument
            //
            // Why not always pass filename argument? Because java
            // can not encode process parameters to utf8.
            // So you can only pass english filenames as arguments
            // to starting mpv instances.
            // Because of that runmpv always opens mpv first and
            // sends a name of video file through controlling pipe
            // second.
            arguments.add("--player-operation-mode=pseudo-gui");

            // Argument is needed so that mpv could open control pipe
            // where runmpv would write commands.
            arguments.add("--input-ipc-server=" + config.pipeName());

            if (config.mpvLogFile() != null) {
                // File, where mpv.exe writes its logs
                arguments.add("--log-file=" + config.mpvLogFile());
            }
            final ProcessBuilder processBuilder = new ProcessBuilder(arguments);

            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            try {
                processBuilder.start();
            } catch (IOException e) {
                if (e.getMessage().contains("CreateProcess error=2")) {
                    logger.severe(e.getMessage());
                    logger.severe(() -> "Couldn't launch mpv, because executable couldn't be found at path - "
                        + mpvExecutable);
                }
                throw new RuntimeException(e);
            }
        }

        FileChannel mpvPipeChannel = null;

        final long start = System.nanoTime();
        boolean waitTimeOver = false;

        // wait until mpv is started and communication pipe is open
        boolean mpvStarted = false;
        while (!mpvStarted && !waitTimeOver) {
            try {
                mpvPipeChannel = FileChannel.open(Path.of(WINDOWS_PIPE_PREFIX).resolve(config.pipeName()), StandardOpenOption.READ, StandardOpenOption.WRITE);
                mpvStarted = true;
            } catch (IOException e) {
                sleep(5);
                final long current = System.nanoTime();
                final long interval = current - start;
                if (interval > config.waitSeconds() * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
        }
        this.queue = new GenericMpvMessageQueue(mpvPipeChannel, mpvPipeChannel);

        if (waitTimeOver) {
            logger.warning("Waited more than " + config.waitSeconds());
            throw new RuntimeException("Couldn't wait until mpv started");
        }
    }

    @Override
    public void execute(MpvCommand command) {
        queue.send(command.content());
    }

    /**
     * Current solution uses wscript from here
     * <a href="https://stackoverflow.com/a/56122113">https://stackoverflow.com/a/56122113</a>
     *
     * It would probably be more reliable to use PowerShell
     * <a href="https://stackoverflow.com/questions/42566799/how-to-bring-focus-to-window-by-process-name">https://stackoverflow.com/questions/42566799/how-to-bring-focus-to-window-by-process-name</a>
     * <a href="https://stackoverflow.com/a/58548853">https://stackoverflow.com/a/58548853</a>
     * but it doesn't work unless admin gave user an explicit permission
     */
    @Override
    public void focus() {
        if (firstLaunch) {
            return;
        }
        final String pid = getProperty("pid");
        logger.info("pid is " + pid);
        final List<String> focusArgs = List.of(
            "wscript",
            "/B",
            config.executableDir() + "/focus.vbs",
            pid
        );

        try {
            commandLine.runOrThrow(String.join(" ", focusArgs));
        } catch (RuntimeException e) {
            logger.log(Level.INFO, "Couldn't focus mpv window", e);
        }
    }

    public String getProperty(String name) {
        final GetProperty command = new GetProperty(name);
        execute(command);
        final long start = System.nanoTime();
        boolean waitTimeOver = false;
        while (!waitTimeOver) {
            final Optional<String> rawResponse = queue.nextMessage();
            rawResponse.ifPresent(l -> logger.fine("mpv msg: " + l));
            final Optional<String> requestLine = rawResponse
                .filter(l -> l.contains("" + command.requestId()))
                .map(l -> {
                    final int startIdx = l.indexOf(":");
                    final int endIdx = l.indexOf(",");
                    final String pid = l.substring(startIdx + 1, endIdx).replaceAll("\"", "");
                    return pid;
                });
            boolean requestFound = requestLine.isPresent();
            if (requestFound) {
                return requestLine.orElseThrow();
            }
            if (rawResponse.isEmpty()) {
                sleep(5);
                final long current = System.nanoTime();
                final long interval = current - start;
                if (interval > (long) 4 * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
        }
        logger.severe("Couldn't wait for response");
        throw new RuntimeException("Couldn't wait for response");
    }
}