package com.evilcorp.mpv.communication;

import java.io.IOException;

public class SyncServerCommunicationChannel {
    private PermanentServerByteChannel channel;
    private final String address;
    private final int port;

    public SyncServerCommunicationChannel(String address, int port) {
        this.address = address;
        this.port = port;
        this.channel = new PermanentServerByteChannel(address, port);
    }

    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    public PermanentServerByteChannel channel() {
        return channel;
    }

    public void attach() {
        if (isOpen()) {
            return;
        }
        channel.reconnect();
    }

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
}
