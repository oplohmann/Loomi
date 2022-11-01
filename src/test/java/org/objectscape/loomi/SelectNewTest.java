package org.objectscape.loomi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.objectscape.loomi.core.ChannelWithHooksForTest;
import org.objectscape.loomi.utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectscape.loomi.Loomi.*;
import static org.objectscape.loomi.utils.TestUtils.sleep;

public class SelectNewTest {

    @Test
    void selectWithDefaultExitImmediatelyAfterDefaultExecuted() {

        var channel1 = new ChannelWithHooksForTest<Integer>();
        var sendChannel1 = channel1.sendChannel();
        var receiveChannel1 = channel1.receiveChannel();

        var channel2 = new ChannelWithHooksForTest<Integer>();
        var sendChannel2 = channel2.sendChannel();
        var receiveChannel2 = channel2.receiveChannel();

        var list = new ConcurrentLinkedQueue<Integer>();

        startVirtual(() -> {
            sendChannel1.send(1);
            sleep(50);
            sendChannel2.send(2);
        });

        for (int i = 0; i < 3; i++) {
            selectNew(selection -> {
                sleep(100);

                receiveChannel1.onReceiveNew(selection, element -> {
                    System.out.println("Channel 1 received element: " + element);
                    list.add(element);
                });

                /*
                TODO: test case that does again receiveChannel1.onReceiveNew --> must be rejected
                 */

                receiveChannel2.onReceiveNew(selection, element -> {
                    System.out.println("Channel 2 received element: " + element);
                    list.add(element);
                });

                // TODO: test case that exception is thrown in case that more than 1 default block is defined by the user.
                selection.onDefault(() -> {
                    System.out.println("No element received");
                    list.add(0);
                });

            });
        }

        assertEquals(3, list.size());
        assertTrue(list.contains(1));
        assertTrue(list.contains(2));
        assertTrue(list.contains(0));
    }

}
