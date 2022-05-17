package com.evilcorp.mpv.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

public class PermanentServerByteChannel implements WritableByteChannel, ReadableByteChannel {
    private final String address;
    private final int port;
    private SocketChannel channel;
    private long lastPing = System.nanoTime();
    private final ByteBuffer ping = StandardCharsets.UTF_8.encode("ping");

    public PermanentServerByteChannel(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void reconnect() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            final SocketChannel channel = SocketChannel.open(new InetSocketAddress(address, port));
            channel.configureBlocking(false);
            this.channel = channel;
        } catch (IOException ignored) { }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        final long now = System.nanoTime();
        if (now - lastPing > 3_000_000_000L) {
            try {
                channel.write(ping);
                ping.clear();
            } catch (IOException e) {
                reconnect();
            }
            lastPing = now;
        }
        try {
            return channel.read(dst);
        } catch (IOException e) {
            reconnect();
        }
        return 0;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        for (int i = 0; i < 3; i++) {
            try {
                return channel.write(src);
            } catch (IOException e) {
                reconnect();
            }
        }
        return 0;
    }

    @Override
    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }
}
