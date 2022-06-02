package com.evilcorp.mpv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenericMpvIncomingMessagesTest {
    WritableByteChannel out = Channels.newChannel(new ByteArrayOutputStream(1000));
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(1000);
    ByteArrayInputStream inStream;
    ReadableByteChannel in;

    MpvMessageQueue queue;
    @BeforeEach
    public void setUp() {
        PrintWriter printWriter = new PrintWriter(outStream);
        printWriter.println("hi");
        printWriter.println("mi");
        printWriter.flush();
        inStream = new ByteArrayInputStream(outStream.toByteArray());
        in = Channels.newChannel(inStream);
        queue = new GenericMpvMessageQueue(out, in);
    }

    @Test
    public void sendMessage() {
        String msg = "test command";
        queue.send(msg);
    }

    @Test
    public void receiveMessage() {
        Optional<String> msg = queue.nextMessage();
        assertFalse(msg.isEmpty());
        assertEquals("hi", msg.orElseThrow());
    }
}