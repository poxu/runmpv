package com.evilcorp.mpv.communication;

import com.evilcorp.mpv.WriteTimeoutExceededException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This Channel puts cap on maximum read and write timeout.
 *
 * If read operation couldn't be completed within timeout,
 * {@link FixedTimeoutByteChannel#read(ByteBuffer)} method returns 0.
 *
 * If write operations couldn't be completed within timeout,
 * {@link FixedTimeoutByteChannel#write(ByteBuffer)} throws
 * {@link WriteTimeoutExceededException}. This implementation should only wrap
 * channels, which write quickly.
 *
 * This implementation uses single thread executor, so it's not possible to
 * read and write at the same time.
 *
 * If write operations is in progress, read will wait safely until it completes.
 *
 * But if read operation is in progress and is not completed within timeout,
 * then {@link FixedTimeoutByteChannel#write(ByteBuffer)} will throw an exception.
 *
 * So either non-blocking reads should be implemented, or two threads should be
 * used.
 *
 * Non-blocking reads require {@link java.io.InputStream} or
 * {@link java.nio.channels.FileChannel}. It's convenient to use
 * {@link ReadableByteChannel}, because both {@link java.nio.channels.SocketChannel}
 * and {@link java.nio.channels.FileChannel} implement it.
 *
 * Also I am not sure how {@link java.nio.channels.SocketChannel} and
 * {@link java.nio.channels.FileChannel} behaves with simultaneous reads and
 * writes.
 *
 * So further research is needed.
 */
public class FixedTimeoutByteChannel implements ReadableByteChannel, WritableByteChannel {
    private final long timeoutMillis;
    private final ReadableByteChannel in;
    private final WritableByteChannel out;
    private Future<Integer> readOp;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ByteBuffer readBuffer = ByteBuffer.allocate(1000);

    /**
     * Constructor, which accepts a single object, implementing
     * {@link ReadableByteChannel} and {@link WritableByteChannel}.
     *
     * It's very convenient to use with bidirectional pipes and sockets.
     * So far it's the only use case for a class.
     *
     * @param channel the channel, which does actual work
     * @param <CHANNEL> type, implementing both {@link ReadableByteChannel} and
     *                 {@link WritableByteChannel}
     */
    public <CHANNEL extends ReadableByteChannel & WritableByteChannel>
        FixedTimeoutByteChannel(CHANNEL channel, int timeout) {
        this(channel, channel, timeout);
    }

    public FixedTimeoutByteChannel(ReadableByteChannel in, WritableByteChannel out, int timeout) {
        this.in = in;
        this.out = out;
        this.timeoutMillis = timeout;
    }

    /**
     * This implementation uses single thread executor to call underlying
     * channel's read method is a separate thread. If read isn't complete
     * within timeout, method returns, that 0 bytes read and doesn't interrupt
     * reading.
     *
     * Value, read from channel, will probably be returned when read method
     * is called next time.
     *
     * @param dst
     *         The buffer into which bytes are to be transferred
     *
     * @return number of bytes read. 0 if nothing is read or read operation
     * took more, than timeout
     * @throws IOException exception, underlying channel could throw
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (in instanceof FileChannel && ((FileChannel) in).size() == 0) {
            return 0;
        }
        if (readOp == null) {
            readOp = executor.submit(() -> in.read(readBuffer));
        }
        try {
            final Integer bytesRead = readOp.get(timeoutMillis,
                TimeUnit.MILLISECONDS);
            if (bytesRead > 0) {
                readBuffer.flip();
                dst.put(readBuffer);
                readBuffer.clear();
            }
            readOp = null;
            return bytesRead;
        } catch (InterruptedException | ExecutionException e) {
            executor.shutdown();
            throw new DelayedChannelOperationException("Exception reading", e);
        } catch (TimeoutException e) {
            return 0;
        }
    }

    /**
     * This implementation uses single thread executor. If write isn't complete
     * within timeout, then {@link WriteTimeoutExceededException} exception is
     * thrown.
     *
     * If read operation is in progress, then write operation will wait until
     * it's complete. Waiting period is included in timeout.
     *
     * @param src
     *         The buffer from which bytes are to be retrieved
     *
     * @return number of bytes written
     * @throws IOException, WriteTimeoutExceededException
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        if (readOp != null) {
            executor.shutdown();
            throw new RuntimeException("nO readwrite");
        }
        try {
            return executor.submit(() -> out.write(src))
                .get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            executor.shutdown();
            throw new DelayedChannelOperationException("Exception writing", e);
        } catch (TimeoutException e) {
            out.close();
            executor.shutdown();
            throw new WriteTimeoutExceededException("" +
                "Couldn't wait until content was written to channel. " +
                "This implementation is intended not to be used if write " +
                "can block for long", e);
        }
    }

    @Override
    public boolean isOpen() {
        return in.isOpen() && out.isOpen();
    }

    @Override
    public void close() throws IOException {
        in.close();
        if (out != in) {
            out.close();
        }
        executor.shutdownNow();
    }
}
