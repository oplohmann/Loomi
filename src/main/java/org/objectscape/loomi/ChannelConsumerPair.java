package org.objectscape.loomi;

import java.util.function.Consumer;

public class ChannelConsumerPair<E> {

    private final Channel<E> channel;
    private final Consumer<E> consumer;

    public ChannelConsumerPair(Channel<E> channel, Consumer<E> consumer) {
        this.channel = channel;
        this.consumer = consumer;
    }

    public Channel<E> getChannel() {
        return channel;
    }

    public Consumer<E> getConsumer() {
        return consumer;
    }

}
