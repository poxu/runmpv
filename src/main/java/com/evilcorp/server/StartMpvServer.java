package com.evilcorp.server;

import com.evilcorp.server.settings.RunMpvServerPropertiesFromSettings;
import com.evilcorp.settings.TextFileSettings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class StartMpvServer {
    public static void main(String[] args) throws IOException {
        final var config = new RunMpvServerPropertiesFromSettings(
            new TextFileSettings("runmpvserver.properties"));
        Selector selector = Selector.open();
        final ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(config.port()));
        server.configureBlocking(false);
        final SelectionKey serverKey = server.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buff = ByteBuffer.allocate(1000);

        while (true) {
            selector.select();
            final Set<SelectionKey> keys = selector.selectedKeys();
            for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext(); ) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    final SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }
                if (key.isReadable()) {
                    final SocketChannel client = (SocketChannel) key.channel();
                    System.out.println("reading");
                    final int readCode = client.read(buff);
                    if (readCode == -1) {
                        client.close();
                        continue;
                    }
                    buff.flip();
                    final String message = StandardCharsets.UTF_8.decode(buff).toString();
                    buff.clear();
                    System.out.println(message.length() + " '" + message + "'");
                    client.write(StandardCharsets.UTF_8.encode("received\n"));
                    for (SelectionKey selectionKey : selector.keys()) {
                        if (selectionKey != key && selectionKey != serverKey) {
                            try {
                                ((SocketChannel) selectionKey.channel())
                                        .write(StandardCharsets.UTF_8.encode(message));
                            } catch (IOException e) {
                                e.printStackTrace();
                                //message lost, but at least server didn't drop dead
                            }
                        }
                    }
                }
                iterator.remove();
            }
        }
    }
}