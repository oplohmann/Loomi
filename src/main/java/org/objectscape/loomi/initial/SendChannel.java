package org.objectscape.loomi.initial;

public class SendChannel<E> extends UniDirectionalChannel<E> {

    public SendChannel(Channel<E> channel) {
        super(channel);
    }

    public void send(E element) throws ChannelClosedException, ChannelInterruptedException {
        channel.send(element);
    }

}
