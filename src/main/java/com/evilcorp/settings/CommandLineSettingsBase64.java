package com.evilcorp.settings;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

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
    private final String[] args;
    private final Base64.Decoder decoder = Base64.getDecoder();

    public CommandLineSettingsBase64(String[] args) {
        this.args = args;
    }

    @Override
    public Optional<String> setting(String name) {
        if ("videoFile".equals(name)) {
            if (args.length == 0) {
                return Optional.empty();
            }
            try {
                final byte[] decodedBytes = decoder.decode(args[args.length - 1]);
                final String filename = StandardCharsets.UTF_8.decode(
                    ByteBuffer.wrap(decodedBytes)).toString();
                return Optional.of(filename.trim());
            } catch (Exception ignore) {
                return Optional.empty();
            }
        }
        for (String argument : args) {
            final String arg;
            try {
                final byte[] decodedBytes = decoder.decode(argument);
                arg = StandardCharsets.UTF_8.decode(
                    ByteBuffer.wrap(decodedBytes)).toString().trim();
            } catch (Exception e) {
                continue;
            }
            final String argPrefix = "--" + name + "=";
            if (arg.length() > argPrefix.length() && arg.startsWith(argPrefix)) {
                return Optional.of(arg.substring(argPrefix.length()));
            }
        }
        return Optional.empty();
    }
}
