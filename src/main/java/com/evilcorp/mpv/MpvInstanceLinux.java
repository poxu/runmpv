package com.evilcorp.mpv;

import com.evilcorp.settings.MpvRunnerProperties;

import java.io.File;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.evilcorp.util.Shortcuts.sleep;

public class MpvInstanceLinux implements MpvInstance {
    public static final String LINUX_PIPE_PREFIX = "./pipe-";
    private final Logger logger;
    private final MpvRunnerProperties config;
    private final SocketChannel channel;

    public MpvInstanceLinux(MpvRunnerProperties config) {
        this.config = config;
        logger = Logger.getLogger(MpvInstanceLinux.class.getName());

        final File mpvPipe = new File(LINUX_PIPE_PREFIX + config.pipeName());

        boolean firstLaunch;
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(mpvPipe.getPath());
        try {
            SocketChannel channel = SocketChannel.open(address);
            channel.close();
            firstLaunch = false;
        } catch (IOException e) {
            firstLaunch = true;
        }

        if (firstLaunch) {
            System.out.println("launching for the first time");
            List<String> arguments = new ArrayList<>();

            final String mpvExecutable = "mpv";
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
            // Because of that rumpv always opens mpv first and
            // sends a name of video file through controlling pipe
            // second.
            arguments.add("--player-operation-mode=pseudo-gui");

            // Argument is needed so that mpv could open control pipe
            // where runmpv would write commands.
            arguments.add("--input-ipc-server=" + LINUX_PIPE_PREFIX + config.pipeName());
            arguments.add("--title=runmpv_win_" + config.pipeName());

            if (config.mpvLogFile() != null) {
                // File, where mpv.exe writes it's logs
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
                    throw new RuntimeException(e);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        final long start = System.nanoTime();
        boolean waitTimeOver = false;

        SocketChannel channel = null;
        // wait until mpv is started and communication pipe is open
        boolean mpvStarted = false;
        while (!mpvStarted && !waitTimeOver) {
            try {
                channel = SocketChannel.open(address);
                mpvStarted = true;
            } catch (IOException e) {
                sleep(5);
                final long current = System.nanoTime();
                final long interval = current - start;
                if (interval > (long)config.waitSeconds() * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
        }
        this.channel = channel;

        if (waitTimeOver) {
            logger.warning("Waited more than " + config.waitSeconds());
            throw new RuntimeException("Couldn't wait until mpv started");
        }
    }

    @Override
    public void execute(MpvCommand command) {
        final ByteBuffer utf8Bytes = StandardCharsets.UTF_8.encode(command.content() + System.lineSeparator());
        try {
            channel.write(utf8Bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void focus() {
        final List<String> focusArgs = List.of(
                "cscript",
                "/B",
                config.executableDir() + "/focus.js",
                "runmpv_win_" + config.pipeName()
        );
        /*
        final ProcessBuilder processBuilder = new ProcessBuilder(focusArgs);

        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
    }
}
