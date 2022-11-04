package org.objectscape.loomi;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ReceiveChannel<E> extends UniDirectionalChannel<E> {

    private static AtomicLong LastId = new AtomicLong(0);

    private final long id = LastId.getAndIncrement();
    public ReceiveChannel(Channel<E> channel) {
        super(channel);
    }

    public ChannelElement<E> receive() throws ChannelInterruptedException {
        return channel.receive();
    }

    public void onReceive(ChannelSelection selection, Consumer<E> elementConsumer) {
        if(selection.isDone()) {
            return;
        }

        channel.closedLock.readLock().lock();

        try {

            selection.addChannel(this);

            if(channel.closed) {
                selection.channelClosed(this);
            }

            ChannelElement<E> channelElement = channel.queue.poll();
            if(channelElement == null) {
                selection.channelEmpty(this);
            } else if(channelElement.isChannelEmptyAndClosed()) {
                selection.channelEmpty(this);
            } else {
                elementConsumer.accept(channelElement.get());
                selection.done();
            }
        } finally {
            channel.closedLock.readLock().unlock();
        }

        if(selection.isNoChannelsDefined()) {
            return;
        }
    }

    public void onReceiveNew(ChannelSelectionNew selection, Consumer<E> elementConsumer) {
        // Don't immediately invoke elementConsumer if channel has an element, because
        // in case of several non-empty channels which one is selected must be non-deterministic.
        var isNonEmpty = channel.addSendListener(selection);
        selection.storeChannel(channel, elementConsumer, isNonEmpty);
    }

    public void forEach(Consumer<? super E> action) {
        while(true) {
            var channelElement = receive();
            if(channelElement.isChannelEmptyAndClosed()) {
                return;
            }
            action.accept(channelElement.get());
        }
    }

    public boolean addSendListener(ChannelSelection selection) {
        channel.closedLock.readLock().lock();

        try {
            channel.addSendListener(selection);
            return !channel.queue.isEmpty();
        } finally {
            channel.closedLock.readLock().unlock();
        }
    }

}
