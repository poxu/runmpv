package com.evilcorp.mpv;

import com.evilcorp.json.MpvIncomingMessages;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GenericMpvMessageQueue implements com.evilcorp.mpv.MpvMessageQueue {
    private final WritableByteChannel out;
    private final ReadableByteChannel in;
    private final ByteBuffer buffer = ByteBuffer.allocate(1000);
    private final MpvIncomingMessages mpvIncomingMessages = new MpvIncomingMessages();

    public GenericMpvMessageQueue(WritableByteChannel out, ReadableByteChannel in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void send(String msg) {
        final ByteBuffer utf8Bytes = StandardCharsets.UTF_8.encode(msg + System.lineSeparator());
        try {
            out.write(utf8Bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> nextMessage() {
        if (mpvIncomingMessages.hasMore()) {
            return mpvIncomingMessages.nextLine();
        }
        try {
            final int bytesRead = in.read(buffer);
            if (bytesRead <= 0) {
                return Optional.empty();
            }
            buffer.flip();
            mpvIncomingMessages.consume(buffer);
            buffer.clear();
            return mpvIncomingMessages.nextLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // A small logging system to diagnose why real logging system fails
    public static void rerouteSystemOutStream(String logfile) {
        final OutputStream out;
        try {
            out = new FileOutputStream(logfile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        PrintStream printWriter = new PrintStream(out);
        System.out.close();
        System.err.close();
        System.setOut(printWriter);
        System.setErr(printWriter);
    }
}
