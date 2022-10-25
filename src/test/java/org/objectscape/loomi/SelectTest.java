package org.objectscape.loomi;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectscape.loomi.core.ChannelWithHooksForTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectscape.loomi.Loomi.select;

public class SelectTest {

    @Test
    void selectEmpty() {

        var channel1 = new ChannelWithHooksForTest<Integer>();
        var sendChannel1 = channel1.sendChannel();
        var receiveChannel1 = channel1.receiveChannel();

        var channel2 = new ChannelWithHooksForTest<Integer>();
        var sendChannel2 = channel2.sendChannel();
        var receiveChannel2 = channel2.receiveChannel();

        var list = new ConcurrentLinkedQueue<Integer>();

        select(selection -> {

            receiveChannel1.onReceive(selection, (item) -> {
                list.add(item);
            });

            receiveChannel2.onReceive(selection, (item) -> {
                list.add(item);
            });

        });

        System.out.println("done");
    }

    @Test
    void selectOneElementEach() {

        var channel1 = new ChannelWithHooksForTest<Integer>();
        var sendChannel1 = channel1.sendChannel();
        var receiveChannel1 = channel1.receiveChannel();

        var channel2 = new ChannelWithHooksForTest<Integer>();
        var sendChannel2 = channel2.sendChannel();
        var receiveChannel2 = channel2.receiveChannel();

        var list = new ConcurrentLinkedQueue<Integer>();

        sendChannel1.send(1);
        sendChannel2.send(2);

        channel1.close();
        channel2.close();

        select(selection -> {
            receiveChannel1.onReceive(selection, (item) -> list.add(item));
            receiveChannel2.onReceive(selection, (item) -> list.add(item));
        });

        assertEquals(2, list.size());
        assertTrue(list.contains(1));
        assertTrue(list.contains(2));
    }

}
