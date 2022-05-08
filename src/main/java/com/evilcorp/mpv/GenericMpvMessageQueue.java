package com.evilcorp.mpv;

import com.evilcorp.json.MpvIncomingMessages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GenericMpvMessageQueue implements com.evilcorp.mpv.MpvMessageQueue {
    private final WritableByteChannel out;
    private final ReadableByteChannel in;
    final ByteBuffer buffer = ByteBuffer.allocate(1000);
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
}
