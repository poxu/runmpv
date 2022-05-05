package com.evilcorp.mpv;

import com.evilcorp.cmd.CommandLine;
import com.evilcorp.cmd.StandardCommandLine;
import com.evilcorp.settings.RunMpvProperties;
import com.evilcorp.util.Shortcuts;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
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

    public MpvInstanceLinux(RunMpvProperties config) {
        this.config = config;
        logger = Logger.getLogger(MpvInstanceLinux.class.getName());
        this.commandLine = new StandardCommandLine(config.executableDir(), Collections.emptyMap());
        final String xdgRuntimeDir = System.getenv("XDG_RUNTIME_DIR");
        final Path socketDir;
        if (xdgRuntimeDir != null) {
            socketDir = Path.of(xdgRuntimeDir).resolve("runmpv");
        } else {
            socketDir = Path.of("/tmp").resolve("runmpv");
        }

        Shortcuts.createDirectoryIfNotExists(socketDir);

        final Path mpvSocket = socketDir.resolve(config.pipeName());

        boolean firstLaunch;
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(mpvSocket);
        try {
            ByteChannel channel = SocketChannel.open(address);
            channel.close();
            firstLaunch = false;
        } catch (IOException e) {
            firstLaunch = true;
        }

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
            arguments.add("--input-ipc-server=" + mpvSocket);

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

        final long start = System.nanoTime();
        boolean waitTimeOver = false;

        ByteChannel channel = null;
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
                if (interval > (long) config.waitSeconds() * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
        }
        this.queue = new GenericMpvMessageQueue(channel, channel);

        if (waitTimeOver) {
            logger.warning("Waited more than " + config.waitSeconds());
            throw new RuntimeException("Couldn't wait until mpv started");
        }
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
        final long start = System.nanoTime();
        boolean waitTimeOver = false;
        String wid = null;
        while (!waitTimeOver) {
            try {
                wid = commandLine.singleResultOrThrow("xdotool search --pid " + pid);
                break;
            } catch (RuntimeException e) {
                logger.log(Level.INFO, "Couldn't find mpv pid", e);
                sleep(100);
                final long current = System.nanoTime();
                final long interval = current - start;
                if (interval > (long) 4 * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
        }
        if (waitTimeOver) {
            logger.info("Couldn't wait until mpv starts");
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
