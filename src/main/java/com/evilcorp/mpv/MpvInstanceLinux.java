package com.evilcorp.mpv;

import com.evilcorp.cmd.CommandLine;
import com.evilcorp.cmd.Retry;
import com.evilcorp.cmd.StandardCommandLine;
import com.evilcorp.settings.RunMpvProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class MpvInstanceLinux implements MpvInstance {
    private final Logger logger;
    private final RunMpvProperties config;
    private final MpvMessageQueue queue;
    private final CommandLine commandLine;
    private final Map<Integer, MpvCallback> callbacks = new HashMap<>();

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

    @Override
    public void execute(MpvRequest command, MpvCallback callback) {
        callbacks.put(command.requestId(), callback);
        queue.send(command.content());
    }

    @Override
    public MpvCallback focusCallback() {
        return new FocusMpvLinux(commandLine);
    }

    @Override
    public void receiveMessages() {
        for (
            Optional<String> rawResponse = queue.nextMessage();
            rawResponse.isPresent();
            rawResponse = queue.nextMessage()
        ) {
            rawResponse.ifPresent(l -> logger.fine("mpv msg: " + l));
            final String response = rawResponse.orElseThrow();
            final Optional<Integer> currentRequest = callbacks.keySet().stream()
                .filter(key -> response.contains(key.toString()))
                .findAny();
            if (currentRequest.isEmpty()) {
                return;
            }
            callbacks.get(currentRequest.orElseThrow())
                .execute(rawResponse.orElseThrow());
            callbacks.remove(currentRequest.orElseThrow());
        }
    }

    @Override
    public boolean hasPendingRequests() {
        return !callbacks.isEmpty();
    }
}
