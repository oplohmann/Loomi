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
            }
        } finally {
            channel.closedLock.readLock().unlock();
        }

        if(selection.isEmpty()) {
            return;
        }
    }

}
