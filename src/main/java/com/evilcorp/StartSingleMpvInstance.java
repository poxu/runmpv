package com.evilcorp;

import com.evilcorp.fs.*;
import com.evilcorp.settings.CompositeMpvRunnerProperties;
import com.evilcorp.settings.DefaultMpvRunnerProperties;
import com.evilcorp.settings.MpvRunnerProperties;
import com.evilcorp.settings.MpvRunnerPropertiesFromFile;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class StartSingleMpvInstance {
    private static Logger LOGGER;

    public static final String WINDOWS_PIPE_PREFIX = "\\\\.\\pipe\\";

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        if (args.length < 1) {
            return;
        }

        final FsFile mpvRunnerHomeDir = new MpvRunnerExecutable();
        LogManager.getLogManager().readConfiguration(
                new FileInputStream(mpvRunnerHomeDir.path().toString() + "/logging.properties")
        );
        LOGGER = Logger.getLogger(StartSingleMpvInstance.class.getName());

        final LocalFsPaths fsPaths = new LocalFsPaths(
                new UserHomeDir(),
                mpvRunnerHomeDir,
                new VideoDir()
        );
        final MpvRunnerProperties config = new CompositeMpvRunnerProperties(
                new MpvRunnerPropertiesFromFile(
                        "mpv_runner.properties",
                        fsPaths
                ), new DefaultMpvRunnerProperties(fsPaths)
        );

        if (config.runnerLogFile() != null) {
            rerouteSystemOutStream(config.runnerLogFile());
        }

        final String videoFileName = args[0];

        LOGGER.info("started");
        LOGGER.info("runmpv argument is " + args[0]);

        boolean mpvStarted = false;

        final File mpvPipe = new File(WINDOWS_PIPE_PREFIX + config.pipeName());

        final boolean firstLaunch = !mpvPipe.exists();
        if (firstLaunch) {
            List<String> arguments = new ArrayList<>();

            // Using absolute path to specify executable
            // PATH variable is, therefore ignored.
            arguments.add(config.mpvHomeDir() + "/mpv.exe");

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

            if (config.mpvLogFile() != null) {
                // File, where mpv.exe writes it's logs
                arguments.add("--log-file=" + config.mpvLogFile());
            }
            final ProcessBuilder processBuilder = new ProcessBuilder(arguments);

            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.start();
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
                Thread.sleep(5);
                final long current = System.nanoTime();
                final long interval = current - start;
                if (interval > config.waitSeconds() * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
        }

        if (waitTimeOver) {
            LOGGER.warning("Waited more than " + config.waitSeconds());
            return;
        }

        if (firstLaunch) {
            sendCommand(mpvPipeStream, "set geometry 640x360");
        }
        LOGGER.info("Loading file " + videoFileName);
        final String loadFileCommand = "loadfile   \"" + videoFileName.replaceAll("\\\\", "\\\\\\\\") + "\" replace";
        sendCommand(mpvPipeStream, loadFileCommand);
        mpvPipeStream.close();
    }

    // A small logging system to diagnose why real logging system fails
    private static void rerouteSystemOutStream(String logfile) throws FileNotFoundException {
        final OutputStream out;
        out = new FileOutputStream(logfile);
        PrintStream printWriter = new PrintStream(out);
        System.out.close();
        System.err.close();
        System.setOut(printWriter);
        System.setErr(printWriter);
    }

    private static void sendCommand(FileOutputStream pipe, String command) throws IOException {
        final PrintWriter writer = new PrintWriter(pipe);
        final ByteBuffer utf8Bytes = StandardCharsets.UTF_8.encode(command);
        pipe.write(utf8Bytes.array());
        writer.println();
        writer.flush();
    }

    public static boolean isAscii(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (string.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }
}