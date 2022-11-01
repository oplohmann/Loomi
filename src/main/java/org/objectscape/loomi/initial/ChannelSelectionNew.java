package org.objectscape.loomi.initial;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChannelSelectionNew implements SendListener {

    private final Map<Channel<?>, ChannelSelectPair<?>> channelSelectionPairByChannel = new ConcurrentHashMap<>();

    @Override
    public void notifyItemWasSent(Channel<?> channel) {
        channelSelectionPairByChannel.get(channel).notifyItemWasSent();
        // TODO: signal semaphore to continue with processing
    }

    public <E> void storeChannel(Channel<E> channel, Consumer<E> elementConsumer, boolean isNonEmpty) {
        channelSelectionPairByChannel.put(channel, new ChannelSelectPair<>(elementConsumer, isNonEmpty));
    }

    public Optional<ImmutablePair<Channel<?>, Consumer<?>>> getArbitraryNonEmptyChannel() {
        var nonEmptyChannels = channelSelectionPairByChannel.entrySet().stream()
                .filter(entry -> entry.getValue().wasNonEmpty())
                .collect(Collectors.toList());

        var size = nonEmptyChannels.size();
        if(size == 0) {
            return Optional.empty();
        }
        if(size > 1) {
            // Which channel is picked is non-deterministic. This is important to make sure that there are no
            // re-occurring concurrent execution pattern that could result in hidden concurrency bugs that don't
            // get detected for a long time.

            // "The for loop starts with fresh which and result values on every iteration, and the
            // blocking select statement executes an >arbitrary< case from those that are ready to
            // proceed.". Mark Summerfield, "Programming in Go", Addison-Wesley, 2012, p.212
            Collections.shuffle(nonEmptyChannels);
        }

        var selectedEntry = nonEmptyChannels.get(0);
        return Optional.of(new ImmutablePair<>(selectedEntry.getKey(), selectedEntry.getValue().getElementConsumer()));
    }

    public void clear() {
        // TODO: NYI
    }

    public boolean isLeaveSelect() {
        // TODO: NYI
        return false;
    }

}
