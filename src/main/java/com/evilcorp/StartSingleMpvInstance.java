package com.evilcorp;

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
        final MpvRunnerProperties config = new MpvRunnerProperties(StartSingleMpvInstance.class, "mpv_runner.properties");

        final String videoFileName = args[0];

        final OutputStream out;
        if (config.getRunnerLogFile() != null) {
            out = new FileOutputStream(config.getRunnerLogFile());
        } else {
            out = OutputStream.nullOutputStream();
        }

        // A small logging system to diagnose why real logging system fails
        PrintWriter printWriter = new PrintWriter(out);
        printWriter.println(args[0]);
        printWriter.println(System.getProperty("user.dir"));
        printWriter.println(config.getExecutableDir());

        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream(config.getExecutableDir() + "/logging.properties"));
            LOGGER = Logger.getLogger(StartSingleMpvInstance.class.getName());
            LOGGER.info("started");
        } catch (Exception e) {
            printWriter.println(e.getMessage());
        }

        printWriter.flush();
        printWriter.close();
        out.close();
        boolean mpvStarted = false;

        final File mpvPipe = new File(WINDOWS_PIPE_PREFIX + config.getPipeName());

        if (!mpvPipe.exists()) {
            List<String> arguments = new ArrayList<>();

            // Using absolute path to specify executable
            // PATH variable is, therefore ignored.
            arguments.add(config.getMpvHomeDir() + "/mpv.exe");

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
            arguments.add("--input-ipc-server=" + config.getPipeName());

            if (config.getMpvLogFile() != null) {
                // File, where mpv.exe writes it's logs
                arguments.add("--log-file=" + config.getMpvLogFile());
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
                mpvPipeStream = new FileOutputStream(WINDOWS_PIPE_PREFIX + config.getPipeName());
                mpvStarted = true;
            } catch (Exception e) {
                Thread.sleep(5);
                final long current = System.nanoTime();
                final long interval = current - start;
                if (interval > config.getWaitSeconds() * 1_000_000_000) {
                    waitTimeOver = true;
                }
            }
        }

        if (waitTimeOver) {
            LOGGER.warning("Waited more than " + config.getWaitSeconds());
            return;
        }

        LOGGER.info("Loading file " + videoFileName);
        final String loadFileCommand = "loadfile   \"" + videoFileName.replaceAll("\\\\", "\\\\\\\\") + "\" replace";
//        sendCommand(fileOutputStream, "set geometry 100:100");
        sendCommand(mpvPipeStream, loadFileCommand);
        mpvPipeStream.close();
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