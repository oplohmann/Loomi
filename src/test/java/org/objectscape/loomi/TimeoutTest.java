package org.objectscape.loomi;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeoutTest {

    @Test
    void timeoutOccurred() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var timeoutOccurred = new AtomicBoolean(false);

        Runnable actionAfterTimeout = () -> {
            timeoutOccurred.compareAndSet(false, true);
            latch.countDown();
        };

        var timeout = new Timeout(100, TimeUnit.MILLISECONDS, actionAfterTimeout);
        timeout.start();

        if(!latch.await(1, TimeUnit.SECONDS)) {
            assertTrue(false, "must not get here for timeout to work correctly");
        }

        assertTrue(timeoutOccurred.get());
    }

    @Test
    void noTimeoutOccurred() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var timeoutOccurred = new AtomicBoolean(false);

        Runnable actionAfterTimeout = () -> {
            timeoutOccurred.compareAndSet(false, true);
            latch.countDown();
        };

        var timeout = new Timeout(10, TimeUnit.SECONDS, actionAfterTimeout);
        timeout.start();
        timeout.release();

        var latchTimeoutOccurred = new AtomicBoolean(false);
        if(!latch.await(1, TimeUnit.SECONDS)) {
            latchTimeoutOccurred.compareAndSet(false, true);
        }

        assertFalse(timeoutOccurred.get());
        assertTrue(latchTimeoutOccurred.get());
    }

    @Test
    void restart() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var timeoutCount = new AtomicInteger(0);

        Runnable actionAfterTimeout = () -> {
            timeoutCount.incrementAndGet();
            latch.countDown();
        };

        var timeout = new Timeout(100, TimeUnit.MILLISECONDS, actionAfterTimeout);
        timeout.start();

        Thread.sleep(10);

        assertTrue(timeoutCount.get() == 0);
        timeout.restart();

        if(!latch.await(1, TimeUnit.SECONDS)) {
            assertTrue(false, "must not get here for timeout to work correctly");
        }

        assertTrue(timeoutCount.get() == 1);
    }
}
