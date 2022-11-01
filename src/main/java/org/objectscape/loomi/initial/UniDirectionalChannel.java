package org.objectscape.loomi.initial;

import java.util.function.Consumer;

public abstract class UniDirectionalChannel<E> {

    protected final Channel<E> channel;

    public UniDirectionalChannel(Channel<E> channel) {
        this.channel = channel;
    }

    public void forEach(Consumer<? super E> action) {
    }

}
