package com.evilcorp.cmd;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StandardCommandLine implements CommandLine {
    private final Path rootFsDir;
    private final Map<String, String> env;
    private final ProcessBuilder.Redirect inputType;
    private final ProcessBuilder.Redirect outputType;
    private final ProcessBuilder.Redirect errorType;

    public StandardCommandLine(String rootFsDir, Map<String, String> env) {
        this(
            Path.of(rootFsDir),
            env,
            ProcessBuilder.Redirect.INHERIT,
            ProcessBuilder.Redirect.INHERIT,
            ProcessBuilder.Redirect.INHERIT
        );
    }

    public StandardCommandLine(
            Path rootFsDir,
            Map<String, String> env,
            ProcessBuilder.Redirect inputType,
            ProcessBuilder.Redirect outputType,
            ProcessBuilder.Redirect errorType
    ) {
        this.rootFsDir = rootFsDir;
        this.env = env;
        this.inputType = inputType;
        this.outputType = outputType;
        this.errorType = errorType;
    }

    public StandardCommandLine(String rootFsDir, Map<String, String> env, boolean inheritOutput) {
        this(
            Path.of(rootFsDir),
            env,
            inheritOutput ? ProcessBuilder.Redirect.INHERIT : ProcessBuilder.Redirect.PIPE,
            inheritOutput ? ProcessBuilder.Redirect.INHERIT : ProcessBuilder.Redirect.PIPE,
            inheritOutput ? ProcessBuilder.Redirect.INHERIT : ProcessBuilder.Redirect.PIPE
        );
    }

    @Override
    public CommandLine withFileOutput(Path file) {
        return null;
    }

    @Override
    public CommandLine withoutInheritOutput() {
        return new StandardCommandLine(
                this.rootFsDir.toAbsolutePath(),
                env,
                ProcessBuilder.Redirect.INHERIT,
                ProcessBuilder.Redirect.PIPE,
                ProcessBuilder.Redirect.PIPE
        );
    }

    @Override
    public CommandLine withPipedInput() {
        return new StandardCommandLine(
                this.rootFsDir.toAbsolutePath(),
                env,
                ProcessBuilder.Redirect.PIPE,
                ProcessBuilder.Redirect.INHERIT,
                ProcessBuilder.Redirect.INHERIT
        );
    }

    @Override
    public Process run(List<String> arguments) {
//        List<String> arguments = Arrays.asList(command.replace("$rootFsDir", rootFsDir.toAbsolutePath().toString()).split(" "));
        final ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(rootFsDir.toFile());
        processBuilder.environment().putAll(env);

        processBuilder.redirectError(errorType);
        processBuilder.redirectOutput(outputType);

        try {
            final Process process = processBuilder.start();
            return process;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProcessBuilder processBuilder(String command) {
        List<String> arguments = Arrays.asList(command.replace("$rootFsDir", rootFsDir.toAbsolutePath().toString()).split(" "));
        final ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(rootFsDir.toFile());
        processBuilder.environment().putAll(env);

        processBuilder.redirectError(errorType);
        processBuilder.redirectOutput(outputType);

        return processBuilder;
    }

    @Override
    public Process run(String command) {
        List<String> arguments = Arrays.asList(command.replace("$rootFsDir", rootFsDir.toAbsolutePath().toString()).split(" "));
        final ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(rootFsDir.toFile());
        processBuilder.environment().putAll(env);

        processBuilder.redirectError(errorType);
        processBuilder.redirectOutput(outputType);

        try {
            final Process process = processBuilder.start();
            return process;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runOrThrow(String command) {
        final Process process = run(command);
        try {
            if (process.waitFor() != 0) {
                throw new RuntimeException("Process " + command + " exited with bad code " + process.exitValue());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runOrThrow(List<String> command) {
        final Process process = run(command);
        try {
            if (process.waitFor() != 0) {
                throw new RuntimeException("Process " + command + " exited with bad code " + process.exitValue());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean successful(String command) {
        final Process process = run(command);
        try {
            return process.waitFor() == 0;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String singleResultOrThrow(String command) {
        final Process guessProc = withoutInheritOutput().run(command);
        final TextProcess textProcess = new TextProcess(guessProc);
        final ProcessResult guessResult = textProcess.waitFor();
        return guessResult.singleOutput();
    }
}
