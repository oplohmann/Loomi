package org.objectscape.loomi;

public class ChannelOpenElement<E> implements ChannelElement<E> {

    private final E element;

    protected ChannelOpenElement(E element) {
        this.element = element;
    }
    public boolean isChannelEmptyAndClosed() {
        return false;
    }
    public E get() {
        return element;
    }

}
