package com.evilcorp.json;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MpvIncomingMessagesTest {
    private String json = """
    {"name": "value"}
    {"number": 123}
    {"umber": 123}
    """;
    private byte[] utf8Bytes = StandardCharsets.UTF_8.encode(json).array();

    @Test
    public void consumeHalfFirstLine() {
        MpvIncomingMessages mpvIncomingMessages = new MpvIncomingMessages();
        mpvIncomingMessages.consume(Arrays.copyOfRange(utf8Bytes, 0, 10));
        Optional<String> optLine = mpvIncomingMessages.nextLine();
        assertTrue(optLine.isEmpty());
    }

    @Test
    public void consumeHalfSecondLine() {
        MpvIncomingMessages mpvIncomingMessages = new MpvIncomingMessages();
        mpvIncomingMessages.consume(Arrays.copyOfRange(utf8Bytes, 0, 20));
        Optional<String> optLine = mpvIncomingMessages.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"name\": \"value\"}",optLine.orElseThrow());
    }

    @Test
    public void consumeHalfThirdLine() {
        MpvIncomingMessages mpvIncomingMessages = new MpvIncomingMessages();
        mpvIncomingMessages.consume(Arrays.copyOfRange(utf8Bytes, 0, 35));
        Optional<String> optLine = mpvIncomingMessages.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"name\": \"value\"}",optLine.orElseThrow());
        optLine = mpvIncomingMessages.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"number\": 123}",optLine.orElseThrow());
    }
}
