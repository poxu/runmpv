package com.evilcorp.mpv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedTimeoutByteChannelTest {
    ByteArrayOutputStream wst = new ByteArrayOutputStream(100);
    WritableByteChannel out = Channels.newChannel(wst);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(1000);
    ByteArrayInputStream inStream;
    ReadableByteChannel in;
    FixedTimeoutByteChannel channel;

    @BeforeEach
    void setUp() {
        PrintWriter printWriter = new PrintWriter(outStream);
        printWriter.print("himi");
        printWriter.flush();
        inStream = new ByteArrayInputStream(outStream.toByteArray());
        in = Channels.newChannel(inStream);
        channel = new FixedTimeoutByteChannel(in, out, 4000);
    }

    @Test
    void completedRead() throws IOException {
        final ByteBuffer buff = ByteBuffer.allocate(1000);
        final int bytesRead = channel.read(buff);
        buff.flip();
        assertEquals(4, bytesRead);
        final String line = StandardCharsets.UTF_8.decode(buff).toString();
        assertEquals("himi", line);
    }

    @Test
    void completedWrite() throws IOException {
        final ByteBuffer buff = StandardCharsets.UTF_8.encode("mihi");
        final int bytesRead = channel.write(buff);
        final String line = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(wst.toByteArray())).toString().trim();
        assertEquals(4, bytesRead);
        assertEquals("mihi", line);
    }
}