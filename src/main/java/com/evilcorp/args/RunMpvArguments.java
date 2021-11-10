package com.evilcorp.args;

import com.evilcorp.fs.FsFile;
import com.evilcorp.fs.ManualFsFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public class RunMpvArguments {
    private final String[] args;

    public RunMpvArguments(String[] args) {
        this.args = args;
    }

    public Optional<FsFile> mpvRunnerHome() {
        if (args.length == 2) {
            final String[] arg = args[0].split("=");
            if (arg.length == 0) {
                throw new IllegalArgumentException(
                        "mpvRunnerHome argument has no = sign. " +
                        "It should be formatted as --runmpv-executable-dir=/path/to/runmpv-executable. " +
                        "Currently arguments are " + Arrays.toString(args));
            } else if (!args[0].substring(2).equals("runmpv-executable-dir")) {
                throw new IllegalArgumentException(
                        "mpvRunnerHome argument has be first. But first argument is something else " +
                        "It should be formatted as --runmpv-executable-dir=/path/to/runmpv-executable. " +
                        "Currently arguments are " + Arrays.toString(args));
            }
            return Optional.of(new ManualFsFile(Path.of(arg[1].trim())));
        }
        return Optional.empty();
    }

    public FsFile video() {
        return new ManualFsFile(Path.of(args[args.length - 1]));
    }

    public boolean empty() {
        return args.length == 0;
    }
}
