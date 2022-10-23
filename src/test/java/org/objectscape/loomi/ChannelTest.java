package org.objectscape.loomi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import org.objectscape.loomi.core.ChannelWithHooksForTest;

import static org.objectscape.loomi.Loomi.startVirtual;

public class ChannelTest {

    @Test
    public void runSimple() throws InterruptedException {
        var receivedValues = new ArrayList<Integer>();

        var channel = new ChannelWithHooksForTest<Integer>();
        var sendChannel = channel.sendChannel();
        var receiveChannel = channel.receiveChannel();

        startVirtual(() -> consumer1(receiveChannel, receivedValues));
        startVirtual(() -> consumer2(receiveChannel, receivedValues));
        startVirtual(() -> {
            System.out.println("about to add 123");
            sendChannel.send(123);
            System.out.println("added 123");
        });
        startVirtual(() -> {
            System.out.println("about to add 789");
            sendChannel.send(789);
            System.out.println("added 789");
        });

        Thread.sleep(2_000);
        for (int i = 0; i < 100; i++) {
            sendChannel.send(i);
        }

        channel.close();

        startVirtual(() -> {
            consumerAfterChannelClosed(receiveChannel, receivedValues);
            consumerAfterChannelClosed(receiveChannel, receivedValues);
        });

        Thread.sleep(5_000);
        for (int i = 0; i < 100; i++) {
            if(!receivedValues.contains(i)) {
                System.out.println("failure!");
            }
        }
        System.out.println(channel.isClosed());
    }

    @Test
    public void forEach() throws InterruptedException {
        var receivedValues = new ArrayList<Integer>();

        var channel = new ChannelWithHooksForTest<Integer>();
        var sendChannel = channel.sendChannel();
        var receiveChannel = channel.receiveChannel();

        receiveChannel.forEach(number -> {

        });
    }

    private void consumer1(ReceiveChannel<Integer> channel, List<Integer> receivedValues) throws ChannelInterruptedException {
        while(true) {
            System.out.println("consumer 1 about to do receive");
            var item = channel.receive();
            if(item.isChannelEmptyAndClosed()) {
                System.out.println("done 1");
                return;
            }
            receivedValues.add(item.get());
            System.out.println("consumer 1 received number " + item.get());
        }
    }

    private void consumer2(ReceiveChannel<Integer> channel, List<Integer> receivedValues) throws ChannelInterruptedException {
        while(true) {
            System.out.println("consumer 2 about to do receive");
            var item = channel.receive();
            if(item.isChannelEmptyAndClosed()) {
                System.out.println("done 2");
                return;
            }
            receivedValues.add(item.get());
            System.out.println("consumer 2 received number " + item.get());
        }
    }

    private void consumerAfterChannelClosed(ReceiveChannel<Integer> channel, List<Integer> receivedValues) throws ChannelInterruptedException {
        while(true) {
            System.out.println("consumer 3 after channel closed about to do receive");
            var item = channel.receive();
            if(item.isChannelEmptyAndClosed()) {
                System.out.println("done 3");
                return;
            }
            receivedValues.add(item.get());
            System.out.println("consumer 3 received number " + item.get());
        }
    }

}
