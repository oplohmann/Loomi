package org.objectscape.loomi;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ChannelSelectPair<E> {

    private final AtomicBoolean wasNonEmpty = new AtomicBoolean(false);
    private final Consumer<E> elementConsumer;

    public ChannelSelectPair(Consumer<E> elementConsumer, boolean wasNonEmpty) {
        this.elementConsumer = elementConsumer;
        this.wasNonEmpty.compareAndSet(false, wasNonEmpty);
    }

    public void notifyItemWasSent() {
        wasNonEmpty.compareAndSet(false, true);
    }

    public boolean wasNonEmpty() {
        return wasNonEmpty.get();
    }

    public Consumer<E> getElementConsumer() {
        return elementConsumer;
    }

}
