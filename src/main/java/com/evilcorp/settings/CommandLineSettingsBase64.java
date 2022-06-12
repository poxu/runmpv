package com.evilcorp.settings;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Settings, contained in command line, base64 encoded
 * The whole --settingName=settingValue is encoded
 *
 * Only verbose settings supported, i. e.
 * --waitSeconds=60 is ok
 * -ws 60 form isn't supported yet
 * --version is also not ok
 */
public class CommandLineSettingsBase64 implements SoftSettings {
    private final Map<String, String> settings;
    private final String[] args;
    private final Base64.Decoder decoder = Base64.getDecoder();

    public CommandLineSettingsBase64(String[] args) {
        this.args = args;
        settings = Arrays.stream(args)
            .map(l -> {
                byte[] decodedBytes = decoder.decode(l);
                return StandardCharsets.UTF_8.decode(
                    ByteBuffer.wrap(decodedBytes)).toString().trim();
            })
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
            final byte[] decodedBytes = decoder.decode(args[args.length - 1]);
            final String filename = StandardCharsets.UTF_8.decode(
                ByteBuffer.wrap(decodedBytes)).toString();
            return Optional.of(filename.trim());
        }
        return Optional.ofNullable(settings.get(name));
    }
}
