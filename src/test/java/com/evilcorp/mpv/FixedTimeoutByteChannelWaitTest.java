package com.evilcorp.mpv;

import com.evilcorp.mpv.communication.FixedTimeoutByteChannel;
import com.evilcorp.util.Shortcuts;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedTimeoutByteChannelWaitTest {
    static class DelayedByteChannel implements ReadableByteChannel,
        WritableByteChannel, InterruptibleChannel {
        private final long timeout;
        private volatile boolean interrupted = false;
        private long start = -1;

        DelayedByteChannel(int timeout) {
            this.timeout = timeout;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (start == -1) {
                start = System.nanoTime();
            }
            long time = 0;
            while (time < timeout * 1_000_000_000) {
                if (interrupted) {
                    throw new AsynchronousCloseException();
                }
                Shortcuts.sleep(50);
                final long end = System.nanoTime();
                time = end - start;
            }
            dst.put((byte) 1);
            return 4;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void close() throws IOException {
            interrupted = true;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (start == -1) {
                start = System.nanoTime();
            }
            long time = 0;
            while (time < timeout * 1_000_000_000) {
                if (interrupted) {
                    throw new AsynchronousCloseException();
                }
                Shortcuts.sleep(50);
                final long end = System.nanoTime();
                time = end - start;
            }
            return 4;
        }
    }

    @Nested
    public class ReadTest {
        DelayedByteChannel block = new DelayedByteChannel(1);
        final FixedTimeoutByteChannel channel = new FixedTimeoutByteChannel(block, 500);

        @Test
        void oneStepInProgressRead() throws IOException {
            final long start = System.nanoTime();
            final ByteBuffer buff = ByteBuffer.allocate(1000);
            int bytesRead = channel.read(buff);
            assertEquals(0, bytesRead);

            final long end = System.nanoTime();
            final long time = end - start;
            assertTrue((time / 1_000_000) <= 2500);

            Shortcuts.sleep(500);
            final ByteBuffer secondBuff = ByteBuffer.allocate(1000);
            bytesRead = channel.read(secondBuff);
            final byte byteRead = secondBuff.get(0);
            assertEquals(4, bytesRead);
            assertEquals(1, byteRead);
        }
    }

    @Nested
    class WriteTest {
        final DelayedByteChannel block = new DelayedByteChannel(1);
        final FixedTimeoutByteChannel channel = new FixedTimeoutByteChannel(block, 500);

        @Test
        public void oneStepInProgressWrite() {
            final long start = System.nanoTime();
            final ByteBuffer buff = ByteBuffer.allocate(1000);

            assertThrows(WriteTimeoutExceededException.class,
                () -> channel.write(buff));

            final long end = System.nanoTime();
            final long time = end - start;
            final long microseconds = time / 1_000_000;
            assertTrue(microseconds >= 500);
        }
    }
}