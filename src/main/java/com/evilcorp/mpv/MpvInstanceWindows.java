package com.evilcorp.mpv;

import com.evilcorp.json.MpvJson;
import com.evilcorp.settings.RunMpvProperties;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import static com.evilcorp.util.Shortcuts.sleep;

public class MpvInstanceWindows implements MpvInstance {
    public static final String WINDOWS_PIPE_PREFIX = "\\\\.\\pipe\\";
    private final ByteChannel channel;
    private final Logger logger;
    private final RunMpvProperties config;

    public MpvInstanceWindows(RunMpvProperties config) {
        this.config = config;
        logger = Logger.getLogger(MpvInstanceWindows.class.getName());

        boolean firstLaunch;
        try {
            ByteChannel channel = FileChannel.open(Path.of(WINDOWS_PIPE_PREFIX).resolve(config.pipeName()), StandardOpenOption.READ, StandardOpenOption.WRITE);
            channel.close();
            firstLaunch = false;
        } catch (IOException e) {
            firstLaunch = true;
        }

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
        channel = mpvPipeChannel;

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
            logger.info("executed " + command.content());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Current solution uses wscript from here
     * https://stackoverflow.com/a/56122113
     *
     * It would probably be more reliable to use PowerShell
     * https://stackoverflow.com/questions/42566799/how-to-bring-focus-to-window-by-process-name
     * https://stackoverflow.com/a/58548853
     */
    @Override
    public void focus() {
        final String pid = getProperty("pid");
        logger.info("pid is " + pid);
        final List<String> focusArgs = List.of(
            "wscript",
            "/B",
            config.executableDir() + "/focus.vbs",
            pid
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

    public String getProperty(String name) {
        final int requestId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
        final String request = "{\"command\": [\"get_property\",\"" + name + "\"], \"request_id\": \"" + requestId + "\"  }\n";
        final ByteBuffer utf8Bytes = StandardCharsets.UTF_8.encode(
            request
        );
        final MpvJson cons = new MpvJson();
        final ByteBuffer utf8Read = ByteBuffer.allocate(1000);
        try {
            channel.write(utf8Bytes);
            final long start = System.nanoTime();
            boolean waitTimeOver = false;
            while (!waitTimeOver) {
                final int bytesRead = channel.read(utf8Read);
                logger.info("bytes read is " + bytesRead);

                if (bytesRead == -1) {
                    continue;
                }
                cons.consume(utf8Read);
                utf8Read.clear();
                while (cons.hasMore()) {
                    final Optional<String> requestLine = cons.nextLine()
                        .filter(l -> l.contains("" + requestId))
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
                }
                sleep(5);
                final long current = System.nanoTime();
                final long interval = current - start;
                if (interval > (long) 4 * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
            logger.severe("Couldn't wait for response");
            throw new RuntimeException("Couldn't wait for response");
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}