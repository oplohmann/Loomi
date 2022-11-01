package org.objectscape.loomi;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ChannelSelectItem {

    private final AtomicBoolean wasNonEmpty = new AtomicBoolean(false);
    private Consumer<?> elementConsumer;
    private Channel<?> channel;

    public <E> ChannelSelectItem(Channel<?> channel, Consumer<?> elementConsumer, boolean wasNonEmpty) {
        this.channel = channel;
        this.elementConsumer = elementConsumer;
        this.wasNonEmpty.compareAndSet(false, wasNonEmpty);
    }

    public void notifyItemWasSent() {
        wasNonEmpty.compareAndSet(false, true);
    }

    public boolean wasNonEmpty() {
        return wasNonEmpty.get();
    }

    public Consumer<?> getElementConsumer() {
        return elementConsumer;
    }

    public Channel<?> getChannel() {
        return channel;
    }

    public void clear() {
        elementConsumer = null;
        channel = null;
    }

}
