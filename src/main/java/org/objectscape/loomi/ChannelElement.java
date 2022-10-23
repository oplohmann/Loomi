package org.objectscape.loomi;

public interface ChannelElement<E> {

    boolean isChannelEmptyAndClosed();
    E get();

}
