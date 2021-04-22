package com.evilcorp.mpv;

import com.evilcorp.settings.MpvRunnerProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.evilcorp.util.Shortcuts.sleep;

public class MpvInstanceWindows implements MpvInstance {
    public static final String WINDOWS_PIPE_PREFIX = "\\\\.\\pipe\\";
    private final FileOutputStream controlPipe;
    private final Logger LOGGER;
    private final String executableDir;

    public MpvInstanceWindows(MpvRunnerProperties config) {
        LOGGER = Logger.getLogger(MpvInstanceWindows.class.getName());
        executableDir = config.executableDir();
        boolean mpvStarted = false;

        final File mpvPipe = new File(WINDOWS_PIPE_PREFIX + config.pipeName());

        final boolean firstLaunch = !mpvPipe.exists();
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
            arguments.add("--player-operation-mode=pseudo-gui");

            // Argument is needed so that mpv could open control pipe
            // where runmpv would write commands.
            arguments.add("--input-ipc-server=" + config.pipeName());
            arguments.add("--title=runmpv_win");

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
                    LOGGER.severe(e.getMessage());
                    LOGGER.severe(() -> "Couldn't launch mpv, because executable couldn't be found at path - "
                            + mpvExecutable);
                    throw new RuntimeException(e);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        FileOutputStream mpvPipeStream = null;

        final long start = System.nanoTime();
        boolean waitTimeOver = false;

        // wait until mpv is started and communication pipe is open
        while (!mpvStarted && !waitTimeOver) {
            try {
                mpvPipeStream = new FileOutputStream(WINDOWS_PIPE_PREFIX + config.pipeName());
                mpvStarted = true;
            } catch (Exception e) {
                sleep(5);
                final long current = System.nanoTime();
                final long interval = current - start;
                if (interval > config.waitSeconds() * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
        }
        controlPipe = mpvPipeStream;

        if (waitTimeOver) {
            LOGGER.warning("Waited more than " + config.waitSeconds());
            throw new RuntimeException("Couldn't wait until mpv started");
        }

    }

    @Override
    public void execute(MpvCommand command) {
        final PrintWriter writer = new PrintWriter(controlPipe);
        final ByteBuffer utf8Bytes = StandardCharsets.UTF_8.encode(command.content());
        try {
            controlPipe.write(utf8Bytes.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writer.println();
        writer.flush();
    }

    @Override
    public void focus() {
        final List<String> focusArgs = List.of(
                "cscript",
                "/B",
                executableDir + "/focus.js",
                "runmpv_win"
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
