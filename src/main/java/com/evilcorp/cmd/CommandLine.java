package com.evilcorp.cmd;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface CommandLine {
    CommandLine withFileOutput(Path file);

    CommandLine withoutInheritOutput();

    CommandLine withPipedInput();

    Process run(List<String> arguments);

    ProcessBuilder processBuilder(String command);

    Process run(String command);

    void runOrExecute(String command, Consumer<Exception> code);

    void runOrThrow(List<String> command);

    boolean successful(String command);

    String singleResultOrThrow(String command);
}
