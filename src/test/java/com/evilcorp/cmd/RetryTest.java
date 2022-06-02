package com.evilcorp.cmd;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryTest {
    static class RetryContainer {
        private int retryNum = 0;
        private final int cap;

        RetryContainer(int cap) {
            this.cap = cap;
        }

        public boolean executeTry() {
            retryNum++;
            if (retryNum == cap) {
                return true;
            }
            return false;
        }

        public int retryNum() {
            return retryNum;
        }

        public int cap() {
            return cap;
        }

        public long nanoSecondsSinceStart() {
            return ((long)retryNum()) * 1_000_000_000;
        }
    }

    @Test
    public void runAndFailFirstTime() {
        RetryContainer container = new RetryContainer(2);

        Retry<Boolean> retry = new Retry<>(4, () -> {
            final boolean tryResult = container.executeTry();
            if (tryResult) {
                return Optional.of(tryResult);
            }
            return Optional.empty();
        });

        final Optional<Boolean> run = retry.get();
        assertTrue(run.isPresent());
        assertEquals(2, container.retryNum());
    }

    @Test
    public void runAndFailMostTimes() {
        RetryContainer container = new RetryContainer(4);
        Retry<Boolean> retry = new Retry<>(1,
            () -> {
                final boolean tryResult = container.executeTry();
                if (tryResult) {
                    return Optional.of(tryResult);
                }
                return Optional.empty();
            },
            (timeout) -> { },
            container::nanoSecondsSinceStart
        );

        final Optional<Boolean> run = retry.get();
        assertTrue(run.isEmpty());
        assertEquals(2, container.retryNum());
    }

    @Test
    public void runAndFailAlways() {
        RetryContainer container = new RetryContainer(20);
        Retry<Boolean> retry = new Retry<>(4, Optional::empty, (timeout) -> { },
            () -> {
                container.executeTry();
                return container.nanoSecondsSinceStart();
            }
        );

        final Optional<Boolean> run = retry.get();
        assertTrue(run.isEmpty());
    }

    @Test
    public void runAndSuccessAlways() {
        RetryContainer container = new RetryContainer(2);
        Retry<Boolean> retry = new Retry<>(4,
            () -> Optional.of(Boolean.TRUE),
            (timeout) -> { },
            () -> {
                container.executeTry();
                return container.nanoSecondsSinceStart();
            }
        );

        final Optional<Boolean> run = retry.get();
        assertTrue(run.isPresent());
    }
}