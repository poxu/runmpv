package com.evilcorp.settings;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Settings, contained in command line.
 * Only verbose settings supported, i. e.
 * --waitSeconds=60 is ok
 * -ws 60 form isn't supported yet
 * --version is also not ok
 */
public class CommandLineSettings implements SoftSettings {
    private final Map<String, String> settings;
    private final String[] args;

    public CommandLineSettings(String[] args) {
        this.args = args;
        settings = Arrays.stream(args)
            .filter(l -> l.startsWith("--"))
            .map(l -> l.substring(2))
            .map(l -> l.split("="))
            .filter(s -> s.length == 2)
            .filter(s -> !s[0].isBlank())
            .filter(s -> !s[1].isBlank())
            .map(s -> new String[]{s[0].trim(), s[1].trim()})
            .collect(Collectors.toUnmodifiableMap(s -> s[0], s -> s[1]));
    }

    @Override
    public Optional<String> setting(String name) {
        if ("videoFile".equals(name)) {
            if (args.length == 0) {
                return Optional.empty();
            }
            return Optional.of(args[args.length - 1]);
        }
        return Optional.ofNullable(settings.get(name));
    }
}
