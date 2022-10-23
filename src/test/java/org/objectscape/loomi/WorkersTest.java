package org.objectscape.loomi;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.objectscape.loomi.core.ChannelWithHooksForTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectscape.loomi.Loomi.startVirtual;

public class WorkersTest {

    @Test
    void simpleCoworkers() {
        // taken from https://www.sohamkamani.com/golang/channels/

        var out = new ChannelWithHooksForTest<Integer>();
        var in = new ChannelWithHooksForTest<Integer>();

        startVirtual(() -> multiplyByTwo(in.receiveChannel(), out.sendChannel()));
        startVirtual(() -> multiplyByTwo(in.receiveChannel(), out.sendChannel()));
        startVirtual(() -> multiplyByTwo(in.receiveChannel(), out.sendChannel()));

        startVirtual(() -> {
            in.send(1);
            in.send(2);
            in.send(3);
            in.send(4);
        });

        var receivedElements = new ArrayList<Integer>();

        receivedElements.add(out.receive().get());
        receivedElements.add(out.receive().get());
        receivedElements.add(out.receive().get());
        receivedElements.add(out.receive().get());

        receivedElements.forEach(System.out::println);

        assertEquals(4, receivedElements.size());
        assertTrue(receivedElements.contains(2));
        assertTrue(receivedElements.contains(4));
        assertTrue(receivedElements.contains(6));
        assertTrue(receivedElements.contains(8));

        out.close();
        in.close();

        assertTrue(out.isClosed());
        assertTrue(in.isClosed());

        assertTrue(out.isEmpty());
        assertTrue(in.isEmpty());
    }

    private void multiplyByTwo(ReceiveChannel<Integer> in, SendChannel<Integer> out) {
        while(true) {
            var channelElement = in.receive();
            if(channelElement.isChannelEmptyAndClosed()) {
                return;
            }
            out.send(channelElement.get() * 2);
        }
    }

}
