package com.evilcorp.cmd;

import java.util.List;

public class ProcessResult {
    private final int exitCode;
    private final List<String> output;
    private final List<String> errors;

    ProcessResult(int exitCode, List<String> output, List<String> error) {
        this.exitCode = exitCode;
        this.output = output;
        this.errors = error;
    }

    public int exitCode() {
        return exitCode;
    }

    public List<String> output() {
        return output;
    }

    public List<String> errors() {
        return errors;
    }

    public String singleOutput() {
        if (exitCode != 0) {
            throw new RuntimeException("ProcessResult exitCode is not 0, but " + exitCode + " output " + output + " errors " + errors);
        }
        if (output.size() != 1) {
            throw new RuntimeException("ProcessResult has more than one line " + output + " errors " + errors);
        }
        return output.iterator().next();
    }
}
