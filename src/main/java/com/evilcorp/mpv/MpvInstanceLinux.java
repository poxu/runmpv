package com.evilcorp.mpv;

import com.evilcorp.settings.RunMpvProperties;
import com.evilcorp.util.Shortcuts;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import static com.evilcorp.util.Shortcuts.sleep;

public class MpvInstanceLinux implements MpvInstance {
    private final Logger logger;
    private final RunMpvProperties config;
    private final SocketChannel channel;

    public MpvInstanceLinux(RunMpvProperties config) {
        this.config = config;
        logger = Logger.getLogger(MpvInstanceLinux.class.getName());
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
            SocketChannel channel = SocketChannel.open(address);
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
            arguments.add("--title=runmpv_win_" + config.pipeName());

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
                if (interval > (long) config.waitSeconds() * 1_000_000_000) {
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

    public String getProperty(String name) {
        final int requestId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
        final String request = "{\"command\": [\"get_property\",\"" + name + "\"], \"request_id\": \"" + requestId + "\"  }\n";
        final ByteBuffer utf8Bytes = StandardCharsets.UTF_8.encode(
            request
        );
        final ByteBuffer utf8Read = ByteBuffer.allocate(1000);
        try {
            channel.write(utf8Bytes);
            final int bytesRead = channel.read(utf8Read);
            utf8Read.flip();
            final CharBuffer decoded = StandardCharsets.UTF_8.decode(utf8Read);
            final String msg = decoded.toString();
            final Optional<String> pidMstOpt = Arrays.stream(msg.split("\n"))
                .filter(s -> s.contains(String.valueOf(requestId)))
                .findAny();
            if (pidMstOpt.isEmpty()) {
                logger.severe("Response with pid not found. Probably not ready yet");
            }
            final String pidMsg = pidMstOpt.orElseThrow();
            final int startIdx = pidMsg.indexOf(":");
            final int endIdx = pidMsg.indexOf(",");
            final String pid = pidMsg.substring(startIdx + 1, endIdx).replaceAll("\"", "");
            return pid;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void focus() {
        final String pid = getProperty("pid");
        final List<String> focusArgs = List.of(
            "bash",
            "-c",
            "xdotool windowraise $(xdotool search --pid " + pid + ")"
        );
        final ProcessBuilder processBuilder = new ProcessBuilder(focusArgs);

        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
