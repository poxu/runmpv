package com.evilcorp.build.files;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextFile {
    private final Path file;

    public TextFile(Path file) {
        this.file = file;
    }

    public void replace(String origin, String replacement) {
        final List<String> lines;
        try (Stream<String> ls = Files.lines(file)) {
            lines = ls
                .map(line -> line.replace(origin, replacement))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (PrintStream out = new PrintStream(file.toFile(),
            StandardCharsets.UTF_8)) {
            lines.forEach(out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
