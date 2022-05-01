package com.evilcorp.json;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MpvJsonTest {

    private String json = """
    {"name": "value"}
    {"number": 123}
    {"umber": 123}
    """;
    private byte[] utf8Bytes = StandardCharsets.UTF_8.encode(json).array();

    @Test
    void consumeHalfFirstLine() {
        MpvJson mpvJson = new MpvJson();
        mpvJson.consume(Arrays.copyOfRange(utf8Bytes, 0, 10));
        Optional<String> optLine = mpvJson.nextLine();
        assertTrue(optLine.isEmpty());
    }

    @Test
    void consumeHalfSecondLine() {
        MpvJson mpvJson = new MpvJson();
        mpvJson.consume(Arrays.copyOfRange(utf8Bytes, 0, 20));
        Optional<String> optLine = mpvJson.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"name\": \"value\"}",optLine.orElseThrow());
    }

    @Test
    void consumeHalfThirdLine() {
        MpvJson mpvJson = new MpvJson();
        mpvJson.consume(Arrays.copyOfRange(utf8Bytes, 0, 35));
        Optional<String> optLine = mpvJson.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"name\": \"value\"}",optLine.orElseThrow());
        optLine = mpvJson.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"number\": 123}",optLine.orElseThrow());
    }
}
