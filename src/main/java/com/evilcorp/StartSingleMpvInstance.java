package com.evilcorp;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StartSingleMpvInstance {

    public static final String WINDOWS_PIPE_PREFIX = "\\\\.\\pipe\\";

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        if (args.length < 1) {
           return;
        }
        final MpvRunnerProperties config = new MpvRunnerProperties(StartSingleMpvInstance.class, "mpv_runner.properties");

        final String videoFileName = args[0];

        OutputStream out;
        if (config.getRunnerLogFile() != null) {
            out = new FileOutputStream(config.getRunnerLogFile());
        } else {
            out = OutputStream.nullOutputStream();
        }
        PrintWriter printWriter = new PrintWriter(out);
        printWriter.println(args[0]);
        printWriter.println(System.getProperty("user.dir"));
        printWriter.println(config.getExecutableDir());
        printWriter.flush();
        printWriter.close();
        out.close();

        boolean mpvStarted = false;

        final File mpvPipe = new File(WINDOWS_PIPE_PREFIX + config.getPipeName());

        if (!mpvPipe.exists()) {
            List<String> arguments = new ArrayList<>();
            arguments.add(config.getMpvHomeDir() + "/mpv.exe");
            arguments.add("--player-operation-mode=pseudo-gui");
            arguments.add("--input-ipc-server=" + config.getPipeName());
            if (config.getMpvLogFile() != null) {
                arguments.add("--log-file=" + config.getMpvLogFile());
            }
            final ProcessBuilder processBuilder = new ProcessBuilder(arguments);

            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.start();
        }

        FileOutputStream fileOutputStream = null;

        final long start = System.nanoTime();
        boolean waitTimeOver = false;

        while (!mpvStarted && !waitTimeOver) {
            try {
                fileOutputStream = new FileOutputStream(WINDOWS_PIPE_PREFIX + config.getPipeName());
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
            System.out.println("Waited more than " + config.getWaitSeconds());
            return;
        }

        final String loadFileCommand = "loadfile   \"" + videoFileName.replaceAll("\\\\", "\\\\\\\\") + "\" replace";
//        sendCommand(fileOutputStream, "set geometry 100:100");
        sendCommand(fileOutputStream, loadFileCommand);
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