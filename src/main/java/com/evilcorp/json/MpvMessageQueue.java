package com.evilcorp.json;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

public class MpvMessageQueue {
    ByteBuffer intBytes = ByteBuffer.allocate(1000);
    Queue<String> lines = new ArrayDeque<>();

    public void consume(byte[] bytes) {
        for (byte it : bytes) {
            if (it == '\n') {
                intBytes.flip();
                lines.add(StandardCharsets.UTF_8.decode(intBytes).toString());
                intBytes.clear();
            } else {
                intBytes.put(it);
            }
        }
    }

    public Optional<String> nextLine() {
        if (lines.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(lines.remove());
        }
    }

    public boolean hasMore() {
        return !lines.isEmpty();
    }

    public void consume(ByteBuffer bytes) {
        for (int i = 0; i < bytes.position(); i++) {
            final byte it = bytes.get(i);

            if (it == '\n') {
                intBytes.flip();
                final String nextLine = StandardCharsets.UTF_8.decode(intBytes).toString();
                lines.add(nextLine);
                intBytes.clear();
            } else {
                intBytes.put(it);
            }
        }
    }
}
