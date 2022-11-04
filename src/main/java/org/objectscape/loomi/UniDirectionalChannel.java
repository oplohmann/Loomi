package org.objectscape.loomi;

import java.util.function.Consumer;

public abstract class UniDirectionalChannel<E> {

    protected final Channel<E> channel;

    public UniDirectionalChannel(Channel<E> channel) {
        this.channel = channel;
    }

}
