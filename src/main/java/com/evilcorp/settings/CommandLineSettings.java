package com.evilcorp.settings;

import java.util.Optional;

/**
 * Settings, contained in command line.
 * Only verbose settings supported, i. e.
 * --waitSeconds=60 is ok
 * -ws 60 form isn't supported yet
 * --version is also not ok
 */
public class CommandLineSettings implements SoftSettings {
    private final String[] args;

    public CommandLineSettings(String[] args) {
        this.args = args;
    }

    @Override
    public Optional<String> setting(String name) {
        if ("videoFile".equals(name)) {
            if (args.length == 0) {
                return Optional.empty();
            }
            return Optional.of(args[args.length - 1]);
        }
        for (String argument : args) {
            final String arg = argument.trim();
            final String argPrefix = "--" + name + "=";
            if (arg.length() > argPrefix.length() && arg.startsWith(argPrefix)) {
                return Optional.of(arg.substring(argPrefix.length()));
            }
        }
        return Optional.empty();
    }
}
