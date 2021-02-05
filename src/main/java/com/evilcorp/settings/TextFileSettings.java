package com.evilcorp.settings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

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
                .map(s -> new String[] {s[0].trim(), s[1].trim()})
                .collect(Collectors.toUnmodifiableMap(s -> s[0], s -> s[1]));

    }

    @Override
    public String setting(String name) {
        return settings.get(name);
    }
}
