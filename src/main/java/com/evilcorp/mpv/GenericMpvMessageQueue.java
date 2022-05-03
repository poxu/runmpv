package com.evilcorp.mpv;

import com.evilcorp.json.MpvJson;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GenericMpvMessageQueue implements MpvMessageQueue {
    private final WritableByteChannel out;
    private final ReadableByteChannel in;
    private final MpvJson mpvJson = new MpvJson();

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
        if (mpvJson.hasMore()) {
            return mpvJson.nextLine();
        }
        final ByteBuffer buffer = ByteBuffer.allocate(100);
        try {
            final int bytesRead = in.read(buffer);
            if (bytesRead <= 0) {
                return Optional.empty();
            }
            mpvJson.consume(buffer);
            return mpvJson.nextLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
