package com.evilcorp.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextProcess {
    private final Process process;
    private final List<String> lines = Collections.synchronizedList(new ArrayList<>());

    public TextProcess(Process process) {
        this.process = process;
    }

    public ProcessResult waitFor() {
        final Thread inputThread = new Thread(() -> process.inputReader().lines()
                .forEach(lines::add)
        );
        final List<String> errors = new ArrayList<>();
        final Thread errorThread = new Thread(() -> process.errorReader().lines()
                .forEach(errors::add)
        );
        inputThread.start();
        errorThread.start();
        try {
            inputThread.join();
            errorThread.join();
            final int exitCode = process.waitFor();
            return new ProcessResult(exitCode, lines, errors);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
