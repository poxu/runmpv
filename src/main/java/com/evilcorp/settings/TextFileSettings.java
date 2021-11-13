package com.evilcorp.settings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.evilcorp.util.Shortcuts.getInStream;

/**
 * Settings, contained in a text file.
 * One setting per line, name and value separated by =.
 * Names and value are trimmed.
 * If value is empty or contains only spaces,
 * then property is treated as non existent.
 * <p>
 * Start line with # symbol to comment the line
 * <p>
 * <p>
 * Example:
 * # commented line
 * setting1=value1
 * setting2=value2
 * setting3  =  value3
 */
public class TextFileSettings implements SoftSettings {
    private final Map<String, String> settings;

    public TextFileSettings(InputStream source) {
        final InputStreamReader inputStreamReader = new InputStreamReader(source, StandardCharsets.UTF_8);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        settings = bufferedReader.lines()
            .map(l -> l.split("="))
            .filter(s -> s.length == 2)
            .filter(s -> !s[0].isBlank())
            .filter(s -> !s[1].isBlank())
            .map(s -> new String[]{s[0].trim(), s[1].trim()})
            .collect(Collectors.toUnmodifiableMap(s -> s[0], s -> s[1]));
    }

    public TextFileSettings(String propertyFileName) {
        this(getInStream(propertyFileName));
    }

    @Override
    public Optional<String> setting(String name) {
        return Optional.ofNullable(settings.get(name));
    }
}
