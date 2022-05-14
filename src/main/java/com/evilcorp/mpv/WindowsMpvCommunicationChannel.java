package com.evilcorp.mpv;

import com.evilcorp.settings.RunMpvProperties;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WindowsMpvCommunicationChannel implements MpvCommunicationChannel {
    public static final String WINDOWS_PIPE_PREFIX = "\\\\.\\pipe\\";
    private FixedTimeoutByteChannel channel;
    private final RunMpvProperties config;

    public WindowsMpvCommunicationChannel(RunMpvProperties config) {
        this.config = config;
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
        final Path fullPipeName = Path.of(WINDOWS_PIPE_PREFIX).resolve(config.pipeName());
        try {
            ByteChannel channel = FileChannel.open(fullPipeName,
                StandardOpenOption.READ, StandardOpenOption.WRITE);
            this.channel = new FixedTimeoutByteChannel(channel, 4000);
        } catch (IOException ignored) { }
    }

    @Override
    public void detach() {
        try {
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return config.pipeName();
    }
}
