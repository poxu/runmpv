package com.evilcorp.mpv.communication;

import com.evilcorp.mpv.MpvCommunicationChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class SyncServerCommunicationChannel implements MpvCommunicationChannel {
    private FixedTimeoutByteChannel channel;
    private final String address;
    private final int port;

    public SyncServerCommunicationChannel(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public boolean isOpen() {
        return channel != null;
    }

    @Override
    public FixedTimeoutByteChannel channel() {
        return channel;
    }

    @Override
    public void attach() {
        if (isOpen()) {
            return;
        }
        try {
            final SocketChannel channel = SocketChannel.open(new InetSocketAddress(address, port));
            channel.configureBlocking(false);
            this.channel = new FixedTimeoutByteChannel(channel, 4000);
        } catch (IOException ignored) { }
    }

    @Override
    public void detach() {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return "unnamed";
    }
}
