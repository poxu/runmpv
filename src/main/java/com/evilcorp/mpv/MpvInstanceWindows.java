package com.evilcorp.mpv;

import com.evilcorp.cmd.CommandLine;
import com.evilcorp.cmd.Retry;
import com.evilcorp.cmd.StandardCommandLine;
import com.evilcorp.mpv.callbacks.FocusMpvWindows;
import com.evilcorp.mpv.communication.FixedTimeoutByteChannel;
import com.evilcorp.settings.RunMpvProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class MpvInstanceWindows implements MpvInstance {
    private final Logger logger;
    private final RunMpvProperties config;
    private final CommandLine commandLine;
    private final boolean firstLaunch;

    public MpvInstanceWindows(RunMpvProperties config, MpvCommunicationChannel commChannel) {
        this.config = config;
        logger = Logger.getLogger(MpvInstanceWindows.class.getName());
        this.commandLine = new StandardCommandLine(config.executableDir(), Collections.emptyMap());

        commChannel.attach();
        this.firstLaunch = !commChannel.isOpen();

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
                if (e.getMessage().contains("CreateProcess error=2")) {
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
    }

    @Override
    public MpvCallback focusCallback() {
        return new FocusMpvWindows(commandLine, config);
    }

    @Override
    public boolean firstLaunch() {
        return firstLaunch;
    }
}