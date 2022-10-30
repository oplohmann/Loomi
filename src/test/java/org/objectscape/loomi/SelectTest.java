package org.objectscape.loomi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.objectscape.loomi.core.ChannelWithHooksForTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectscape.loomi.Loomi.select;
import static org.objectscape.loomi.Loomi.startVirtual;

public class SelectTest {

    @Test
    void selectEmpty() {

        var channel1 = new ChannelWithHooksForTest<Integer>();
        var receiveChannel1 = channel1.receiveChannel();

        var channel2 = new ChannelWithHooksForTest<Integer>();
        var receiveChannel2 = channel2.receiveChannel();

        var list = new ConcurrentLinkedQueue<Integer>();
        var deadlockExceptionOccurred = false;

        try {
            select(selection -> {

                receiveChannel1.onReceive(selection, (item) -> {
                    list.add(item);
                });

                receiveChannel2.onReceive(selection, (item) -> {
                    list.add(item);
                });

            });
        } catch (DeadlockException e) {
            deadlockExceptionOccurred = true;
        }

        assertTrue(deadlockExceptionOccurred);
    }

    @Test
    void selectNoChannels() {
        var iterationCount = new AtomicInteger(0);
        select(selection -> {
            // no single channel defined
            iterationCount.getAndIncrement();
        });

        // select exits after one iteration
        assertEquals(1, iterationCount.get());
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

        channel2.close();

        var iterationCount = new AtomicInteger(0);
        var elementsCount = 3;

        for (int i = 0; i < elementsCount; i++) {

            select(selection -> {
                receiveChannel1.onReceive(selection, item -> list.add(item));
                receiveChannel2.onReceive(selection, item -> list.add(item));

                if(iterationCount.get() == 0) {
                    sendChannel1.send(3);
                    channel1.close();
                }

                iterationCount.getAndIncrement();
            });

        }

        assertEquals(3, list.size());
        assertTrue(list.contains(1));
        assertTrue(list.contains(2));
        assertTrue(list.contains(3));
    }

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
            select(selection -> {
                sleep(100);

                receiveChannel1.onReceive(selection, element -> {
                    System.out.println("Channel 1 received element: " + element);
                    list.add(element);
                });

                receiveChannel2.onReceive(selection, element -> {
                    System.out.println("Channel 2 received element: " + element);
                    list.add(element);
                });

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

    @Test
    void selectWithDefaultAndDone() {

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

        select(selection -> {
            sleep(100);

            receiveChannel1.onReceive(selection, element -> {
                System.out.println("Channel 1 received element: " + element);
                list.add(element);
                selection.done();
            });

            receiveChannel2.onReceive(selection, element -> {
                assertTrue(false, "should never be called, because selection.done(); called before");
            });

            selection.onDefault(() -> {
                assertTrue(false, "should never be called, because selection.done(); called before");
            });

        });

        assertEquals(1, list.size());
        assertTrue(list.contains(1));
    }

    @Test
    void exitAfterFirstChannelReceivedInput() {
        // same as 1st example https://golangbot.com/select/
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

        select(selection -> {
            sleep(100);

            receiveChannel1.onReceive(selection, element -> {
                System.out.println("Channel 1 received element: " + element);
                list.add(element);
            });

            receiveChannel2.onReceive(selection, element -> {
                assertTrue(false, "should never be called, because selection.done(); called before");
            });

        });

        assertEquals(1, list.size());
        assertTrue(list.contains(1));
    }

    @Test
    void selectWithDefaultAndReturn() {
        // same as 2nd example https://golangbot.com/select/
        var channel1 = new ChannelWithHooksForTest<Integer>();
        var sendChannel1 = channel1.sendChannel();
        var receiveChannel1 = channel1.receiveChannel();

        var list = new ConcurrentLinkedQueue<Integer>();

        startVirtual(() -> {
            sleep(1050 );
            sendChannel1.send(1);
        });

        var done = new AtomicBoolean(false);

        while(!done.get()) {

            select(selection -> {
                sleep(100);

                receiveChannel1.onReceive(selection, element -> {
                    System.out.println("Channel 1 received element: " + element);
                    list.add(element);
                    selection.done();
                    done.compareAndSet(false, true);
                });

                selection.onDefault(() -> {
                    System.out.println("No element received");
                    list.add(0);
                });

            });

        }


        assertEquals(10, list.size());
        assertTrue(list.contains(1));
        assertEquals(9, list.stream().filter(each -> each == 0).count());
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) { }
    }

}
