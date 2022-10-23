package org.objectscape.loomi;

import java.util.function.Consumer;

public abstract class UniDirectionalChannel<E> {

    protected final Channel channel;

    public UniDirectionalChannel(Channel channel) {
        this.channel = channel;
    }

    public void forEach(Consumer<? super E> action) {
    }
}
