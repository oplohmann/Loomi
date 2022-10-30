package org.objectscape.loomi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
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
        /*
        same as 1st example https://golangbot.com/select/
        package main

        import (
            "fmt"
            "time"
        )

        func server1(ch chan string) {
            time.Sleep(6 * time.Second)
            ch <- "from server1"
        }

        func server2(ch chan string) {
            time.Sleep(3 * time.Second)
            ch <- "from server2"
        }

        func main() {
            output1 := make(chan string)
            output2 := make(chan string)
            go server1(output1)
            go server2(output2)
            select {
                case s1 := <-output1:
                    fmt.Println(s1)
                case s2 := <-output2:
                    fmt.Println(s2)
            }
        }
         */

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
                assertTrue(false, "should never be called, because select statement is finished once any channel received an any");
            });

        });

        assertEquals(1, list.size());
        assertTrue(list.contains(1));
    }

    @Test
    void selectWithDefaultAndReturn() {
        /*
        same as 2nd example here: https://golangbot.com/select/

        package main

        import (
            "fmt"
            "time"
        )

        func process(ch chan string) {
            time.Sleep(10500 * time.Millisecond)
            ch <- "process successful"
        }

        func main() {
            ch := make(chan string)
            go process(ch)
            for {
                time.Sleep(1000 * time.Millisecond)
                select {
                    case v := <-ch:
                        fmt.Println("received value: ", v)
                        return // take note of this return here !!
                    default:
                    fmt.Println("no value received")
                }
            }
        }
         */

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
                    selection.done(); // workaround to have same effect as return
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

    @Test
    void selectWithDefaultsTwoChannels() {

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
    @Disabled // not yet implemented
    void selectWithTimeouts() {
        /*
        Taken from this example: https://gobyexample.com/timeouts (but with unbounded channels)

        c1 := make(chan string)
        go func() {
            time.Sleep(2 * time.Second)
            c1 <- "result 1"
        }()

        select {
            case res := <-c1:
                fmt.Println(res)
            case <-time.After(1 * time.Second):
                fmt.Println("timeout 1")
        }

        c2 := make(chan string)
        go func() {
            time.Sleep(2 * time.Second)
            c2 <- "result 2"
        }()

        select {
            case res := <-c2:
                fmt.Println(res)
            case <-time.After(3 * time.Second):
                fmt.Println("timeout 2")
        }
        */

        var channel1 = new ChannelWithHooksForTest<Integer>();
        var sendChannel1 = channel1.sendChannel();
        var receiveChannel1 = channel1.receiveChannel();

        var list1 = new ConcurrentLinkedQueue<Integer>();
        var listTimeout = new ConcurrentLinkedQueue<Integer>();

        startVirtual(() -> {
            sleep(2000);
            sendChannel1.send(1);
        });

        select(selection -> {

            receiveChannel1.onReceive(selection, element -> {
                System.out.println("Channel 1 received element: " + element);
                list1.add(element);
            });

            selection.onTimeout(1, TimeUnit.SECONDS, () -> {
                System.out.println("timeout period expired");
                listTimeout.add(0);
            });
        });

        System.out.println("done");

    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
