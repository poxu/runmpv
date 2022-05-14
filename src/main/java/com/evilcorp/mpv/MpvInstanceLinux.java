package com.evilcorp.mpv;

import com.evilcorp.cmd.CommandLine;
import com.evilcorp.cmd.Retry;
import com.evilcorp.cmd.StandardCommandLine;
import com.evilcorp.settings.RunMpvProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.evilcorp.util.Shortcuts.sleep;

public class MpvInstanceLinux implements MpvInstance {
    private final Logger logger;
    private final RunMpvProperties config;
    private final MpvMessageQueue queue;
    private final CommandLine commandLine;

    public MpvInstanceLinux(RunMpvProperties config, MpvCommunicationChannel commChannel) {
        this.config = config;
        logger = Logger.getLogger(MpvInstanceLinux.class.getName());
        this.commandLine = new StandardCommandLine(config.executableDir(), Collections.emptyMap());
        commChannel.attach();
        final boolean firstLaunch = !commChannel.isOpen();

        if (firstLaunch) {
            List<String> arguments = new ArrayList<>();

            final String mpvExecutable = "mpv";
            arguments.add(config.mpvHomeDir() + mpvExecutable);

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
            arguments.add("--input-ipc-server=" + commChannel.name());

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
                if (e.getMessage().contains("error=2, , No such file or directory")) {
                    logger.severe(e.getMessage());
                    logger.severe(() -> "Couldn't launch mpv, because executable couldn't be found at path - "
                        + mpvExecutable);
                }
                throw new RuntimeException(e);
            }
        }
        // wait until mpv is started and communication pipe is open
        Retry<FixedTimeoutByteChannel> findMpvChannel = new Retry<>(config.waitSeconds(),
            () -> {
                commChannel.attach();
                if (commChannel.isOpen()) {
                    return Optional.of(commChannel.channel());
                }
                return Optional.empty();
            }
        );
        final Optional<FixedTimeoutByteChannel> channel = findMpvChannel.get();
        if (channel.isEmpty()) {
            logger.warning("Waited more than " + config.waitSeconds());
            throw new RuntimeException("Couldn't wait until mpv started");
        }
        this.queue = new GenericMpvMessageQueue(channel.orElseThrow());
    }

    @Override
    public void execute(MpvCommand command) {
        queue.send(command.content());
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

    @Override
    public void focus() {
        final String pid = getProperty("pid");
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

    @Override
    public void close() {
        queue.close();
    }
}
