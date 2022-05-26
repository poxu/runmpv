package com.evilcorp.settings;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentedTextFileSettingsTest {

    @Nested
    class IgnoresCommentedLines {
        private final String RAW_SETTINGS = """
            # waitSeconds=15
            # waitSeconds=16
            """;

        @Test
        public void commentedLinesAreIgnored() {
            final TextFileSettings settings = new TextFileSettings(new ByteArrayInputStream(RAW_SETTINGS.getBytes()));
            assertTrue(settings.setting("# waitSeconds").isEmpty());
        }
    }
}