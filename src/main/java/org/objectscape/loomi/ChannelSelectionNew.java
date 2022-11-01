package org.objectscape.loomi;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChannelSelectionNew implements SendListener {

    private final Map<Channel<?>, ChannelSelectItem> channelSelectionPairByChannel = new HashMap<>();
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final Object timeoutLock = new Object();
    private boolean timeoutBlockExecuted = false;

    private Runnable defaultRunnable;

    @Override
    public void notifyItemWasSent(Channel<?> channel) {
        synchronized (channelSelectionPairByChannel) {
            channelSelectionPairByChannel.get(channel).notifyItemWasSent();
        }
        // TODO: signal semaphore to continue with processing
    }

    public <E> void storeChannel(Channel<E> channel, Consumer<E> elementConsumer, boolean isNonEmpty) {
        synchronized (channelSelectionPairByChannel) {
            var previousValue = channelSelectionPairByChannel.put(channel, new ChannelSelectItem(channel, elementConsumer, isNonEmpty));
            assert previousValue == null;
        }
    }

    public void clear() {
        synchronized (channelSelectionPairByChannel) {
            channelSelectionPairByChannel.entrySet().forEach(entry -> {
                entry.getKey().removeListener(this);
                entry.getValue().clear();
            });
            channelSelectionPairByChannel.clear();
        }
    }

    public boolean isLeaveSelect() {
        // TODO: NYI
        return false;
    }

    public void onDefault(Runnable runnable) {
        if(defaultRunnable != null) {
            throw new SelectException("onDefault block in select must only be defined once");
        }
        defaultRunnable = runnable;
    }

    void done() {
        done.compareAndSet(false, true);
    }

    /**
     * Calling this method is only valid when the select block has been run already and therefore all channels in
     * the select block have been visited and have been added to <tt>channelSelectionPairByChannel</tt>. Thereafter,
     * no elements will be added or removed anymore.
     *
     * @return
     */
    List<ChannelSelectItem> getChannelSelectItems() {
        var channelInfo = new ArrayList<ChannelSelectItem>();
        synchronized (channelSelectionPairByChannel) {
            channelInfo.addAll(channelSelectionPairByChannel.values());
        }
        return channelInfo;
    }

    boolean tryReceiveOnArbitraryChannel() {
        var channelSelectItems = getChannelSelectItems();
        var size = channelSelectItems.size();

        if(size == 0) {
            return false;
        }

        if(size > 1) {
            // Which channel is picked is non-deterministic. This is important to make sure that there are no
            // re-occurring concurrent execution pattern that could result in hidden concurrency bugs that don't
            // get detected for a long time.

            // "The for loop starts with fresh which and result values on every iteration, and the
            // blocking select statement executes an >arbitrary< case from those that are ready to
            // proceed.". Mark Summerfield, "Programming in Go", Addison-Wesley, 2012, p.212
            Collections.shuffle(channelSelectItems);
        }

        for(var channelSelectItem : channelSelectItems) {
            var channel = channelSelectItem.getChannel();
            Consumer<Object> consumer = (Consumer<Object>) channelSelectItem.getElementConsumer();

            synchronized (timeoutLock) {
                if(timeoutBlockExecuted) {
                    return true;
                }

                var optionalChannelElement = channel.poll();
                if(optionalChannelElement.isEmpty()) {
                    continue;
                }

                var channelElement = optionalChannelElement.get();
                if(!channelElement.isChannelEmptyAndClosed()) {
                    try {
                        consumer.accept(channelElement.get());
                    } finally {
                        done();
                        return true;
                    }
                }

            }
        }

        return false;
    }

    public boolean runDefaultBlock() {
        if(defaultRunnable == null) {
            return false;
        }

        try {
            synchronized (timeoutLock) {
                if(timeoutBlockExecuted) {
                    return false;
                }
                defaultRunnable.run();
            }
        } finally {
            return true;
        }
    }

}
