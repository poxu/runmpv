package com.evilcorp.settings;

import com.evilcorp.fs.FsPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
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

    public TextFileSettings(String propertyFileName, FsPaths fsPaths) {
        this(getInStream(propertyFileName, fsPaths));
    }

    private static InputStream getInStream(String propertyFileName, FsPaths fsPaths) {
        try {
            return new FileInputStream(fsPaths.resolve("%r/" + propertyFileName).path()
                    .toString());
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public Optional<String> setting(String name) {
        return Optional.ofNullable(settings.get(name));
    }
}