package org.objectscape.loomi;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Timeout {

    private long duration;
    private TimeUnit timeUnit;
    private Semaphore timeoutGard = new Semaphore(0);
    private AtomicBoolean cancelled = new AtomicBoolean(false);

    private Runnable actionAfterTimeout;

    public Timeout(long duration, TimeUnit timeUnit, Runnable actionAfterTimeout) {
        this.duration = duration;
        this.timeUnit = timeUnit;
        this.actionAfterTimeout = actionAfterTimeout;
    }

    public void start() {
        Loomi.startVirtual(() -> {
            try {
                if(!timeoutGard.tryAcquire(1, duration, timeUnit)) {
                    if(!cancelled.getAndSet(false)) { // TODO: create test case for this
                        actionAfterTimeout.run();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void restart() {
        synchronized (timeoutGard) {
            cancelled.compareAndSet(false, true);
            timeoutGard.release();
            timeoutGard.drainPermits();
            start();
        }
    }

    protected void release() {
        synchronized (timeoutGard) {
            timeoutGard.release();
        }
    }

}
