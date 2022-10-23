package org.objectscape.loomi;

public class ChannelClosedElement<E> implements ChannelElement<E> {

    private static final ChannelClosedElement<?> INSTANCE = new ChannelClosedElement<>();

    public static <E> ChannelClosedElement<E> getInstance() {
        return (ChannelClosedElement<E>) INSTANCE;
    }

    @Override
    public E get() {
        throw new ChannelClosedException("channel closed");
    }

    @Override
    public boolean isChannelEmptyAndClosed() {
        return true;
    }

}
