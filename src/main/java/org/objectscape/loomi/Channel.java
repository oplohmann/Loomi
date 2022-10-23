package org.objectscape.loomi;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Channel<E> {

    protected final LinkedTransferQueue<ChannelElement<E>> queue = new LinkedTransferQueue<>();
    private final SendChannel<E> sendChannel = new SendChannel<>(this);
    private final ReceiveChannel<E> receiveChannel = new ReceiveChannel<>(this);

    protected final ReentrantReadWriteLock closedLock = new ReentrantReadWriteLock();

    // Once set to false it will never change to true anymore.
    protected boolean closed = false;

    public Channel() {
    }

    public SendChannel<E> sendChannel() {
        return sendChannel;
    }

    public ReceiveChannel<E> receiveChannel() {
        return receiveChannel;
    }

    public void send(E element) throws ChannelClosedException, ChannelInterruptedException {
        closedLock.readLock().lock();
        try {
            if (closed) {
                throw new ChannelClosedException("channel closed");
            }
            queue.add(new ChannelOpenElement(element));
        } finally {
            closedLock.readLock().unlock();
        }
    }

    public ChannelElement<E> receive() throws ChannelClosedException {
        closedLock.readLock().lock();

        try {
            if (closed) {
                var item = queue.poll();
                if(item != null && !item.isChannelEmptyAndClosed()) {
                    return item;
                }

                return ChannelClosedElement.getInstance();
            }
        } finally {
            closedLock.readLock().unlock();
        }

        return queueTake();
    }

    protected ChannelElement<E> queueTake() throws ChannelInterruptedException {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new ChannelInterruptedException(e);
        }
    }

    public synchronized void close() {
        closedLock.writeLock().lock();
        try {
            if (closed) {
                throw new ChannelClosedException("channel already closed");
            }

            closed = true;

            while (true) {
                var waitingConsumerCount = queue.getWaitingConsumerCount();
                // What if a context switch happens right at this line? This is why sending ChannelOptional.closed()
                // to all currently waiting on a blocking take on this channel is enclosed by the closeLock and so
                // is method receive as well.
                waitingConsumerCount(waitingConsumerCount);
                if (waitingConsumerCount == 0) {
                    return;
                }
                for (int i = 0; i < waitingConsumerCount; i++) {
                    queue.add(ChannelClosedElement.getInstance());
                }
            }

        } finally {
            closedLock.writeLock().unlock();
        }
    }

    protected void waitingConsumerCount(int waitingConsumerCount) {
        // redefine in subclass as appropriate
    }

}
