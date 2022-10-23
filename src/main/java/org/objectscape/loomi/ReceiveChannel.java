package org.objectscape.loomi;

public class ReceiveChannel<E> extends UniDirectionalChannel<E> {

    public ReceiveChannel(Channel<E> channel) {
        super(channel);
    }

    public ChannelElement<E> receive() throws ChannelInterruptedException {
        return channel.receive();
    }

}
