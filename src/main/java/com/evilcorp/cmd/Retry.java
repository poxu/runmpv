package com.evilcorp.cmd;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Retry<R> {
    private final int seconds;
    private final Supplier<Optional<R>> code;
    private final Consumer<Long> wait;
    private final Supplier<Long> now;
    private final long timeout = 100;

    public Retry(int seconds, Supplier<Optional<R>> code) {
        this(
            seconds,
            code,
            (timeout) -> {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            },
            System::nanoTime
        );
    }

    public Retry(int seconds, Supplier<Optional<R>> code, Consumer<Long> wait, Supplier<Long> now) {
        this.seconds = seconds;
        this.code = code;
        this.wait = wait;
        this.now = now;
    }

    public Optional<R> get() {
        final long start = now.get();
        while (true) {
            Optional<R> res = code.get();
            if (res.isPresent()) {
                return res;
            }
            wait.accept(timeout);
            final long current = now.get();
            final long interval = current - start;
            if (interval > (long) seconds * 1_000_000_000) {
                return Optional.empty();
            }
        }
    }
}
