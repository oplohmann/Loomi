package org.objectscape.loomi.initial;

public interface ChannelElement<E> {

    boolean isChannelEmptyAndClosed();
    E get();

}
