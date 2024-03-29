package com.evilcorp.mpv.communication;

import com.evilcorp.mpv.MpvCommunicationChannel;
import com.evilcorp.settings.RunMpvProperties;
import com.evilcorp.util.Shortcuts;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

public class LinuxMpvCommunicationChannel implements MpvCommunicationChannel {
    private FixedTimeoutByteChannel channel;
    private final RunMpvProperties config;
    private String name;

    public LinuxMpvCommunicationChannel(RunMpvProperties config) {
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
        final Path mpvSocketPath = Path.of(name());
        Shortcuts.createDirectoryIfNotExists(mpvSocketPath.getParent());

        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(mpvSocketPath);
        try {
            SocketChannel channel = SocketChannel.open(address);
            channel.configureBlocking(false);
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
        if (name != null) {
            return name;
        }
        final Path socketDir = Path.of(config.runmpvTmpDir())
            .resolve("runmpv");
        final Path mpvSocketPath = socketDir.resolve(config.pipeName());
        name = mpvSocketPath.toString();
        return name;
    }
}
