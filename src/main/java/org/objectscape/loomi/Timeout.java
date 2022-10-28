package org.objectscape.loomi;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Timeout {

    private long duration;
    private TimeUnit timeUnit;
    private Semaphore timeoutGard = new Semaphore(0);

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
                    actionAfterTimeout.run();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void restart() {
        timeoutGard.release();
        start();
    }

    public void release() {
        timeoutGard.release();
    }

}
