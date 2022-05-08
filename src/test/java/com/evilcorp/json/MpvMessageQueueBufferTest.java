package com.evilcorp.json;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MpvMessageQueueBufferTest {

    private String json = """
    {"name": "value"}
    {"number": 123}
    {"umber": 123}
    """;
    private byte[] utf8Bytes = StandardCharsets.UTF_8.encode(json).array();

    @Test
    void consumeHalfFirstLine() {
        MpvMessageQueue mpvMessageQueue = new MpvMessageQueue();
        final byte[] inBytes = Arrays.copyOfRange(utf8Bytes, 0, 10);
        final ByteBuffer allocate = ByteBuffer.allocate(1000);
        allocate.put(inBytes);
        mpvMessageQueue.consume(allocate);
        Optional<String> optLine = mpvMessageQueue.nextLine();
        assertTrue(optLine.isEmpty());
    }

    @Test
    void consumeHalfSecondLine() {
        MpvMessageQueue mpvMessageQueue = new MpvMessageQueue();
        final byte[] inBytes = Arrays.copyOfRange(utf8Bytes, 0, 20);
        final ByteBuffer allocate = ByteBuffer.allocate(1000);
        allocate.put(inBytes);
        System.out.println(allocate.position());
        mpvMessageQueue.consume(allocate);
        Optional<String> optLine = mpvMessageQueue.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"name\": \"value\"}",optLine.orElseThrow());
    }

    @Test
    void consumeHalfThirdLine() {
        MpvMessageQueue mpvMessageQueue = new MpvMessageQueue();
        final byte[] inBytes = Arrays.copyOfRange(utf8Bytes, 0, 35);
        final ByteBuffer allocate = ByteBuffer.allocate(1000);
        allocate.put(inBytes);
        System.out.println(allocate.position());
        mpvMessageQueue.consume(allocate);
        Optional<String> optLine = mpvMessageQueue.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"name\": \"value\"}",optLine.orElseThrow());
        optLine = mpvMessageQueue.nextLine();
        assertFalse(optLine.isEmpty());
        assertEquals("{\"number\": 123}",optLine.orElseThrow());
    }
}
