package org.objectscape.loomi;

import java.util.concurrent.Semaphore;

public class BoundChannel<E> extends Channel<E> {

    private final int capacity;
    private final Semaphore capacityGuard;

    public BoundChannel(int capacity) {
        this.capacity = capacity;
        capacityGuard = new Semaphore(capacity);
    }

    public BoundChannel(int capacity, boolean fair) {
        this.capacity = capacity;
        capacityGuard = new Semaphore(capacity, fair);
    }

    @Override
    public void send(E element) throws ChannelClosedException, ChannelInterruptedException {
        try {
            capacityGuard.acquire();
        } catch (InterruptedException e) {
            throw new ChannelInterruptedException(e);
        }

        try {
            super.send(element);
        } catch (ChannelClosedException | ChannelInterruptedException e) {
            capacityGuard.release();
            throw e;
        } catch (Exception e) {
            capacityGuard.release();
            throw e;
        }
    }

    public boolean trySend(E element) throws ChannelClosedException, ChannelInterruptedException {
        if(!capacityGuard.tryAcquire()) {
            return false;
        }

        try {
            super.send(element);
            return true;
        } catch (ChannelClosedException e) {
            capacityGuard.release();
            throw e;
        } catch (ChannelInterruptedException e) {
            capacityGuard.release();
            throw e;
        }
    }

    @Override
    protected ChannelElement<E> queueTake() throws ChannelClosedException {
        try {
            var item = super.queueTake();
            capacityGuard.release();
            return item;
        } catch (ChannelInterruptedException e) {
            throw e;
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isFair() {
        return capacityGuard.isFair();
    }

    public int getAvailableCapacity() {
        return capacityGuard.availablePermits();
    }

    public int getQueueLength() {
        return capacityGuard.getQueueLength();
    }

}
