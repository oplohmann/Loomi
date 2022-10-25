package org.objectscape.loomi;

import java.util.HashSet;
import java.util.Set;

public class ChannelSelection {

    private Set<ReceiveChannel> channelsInSelection = new HashSet<>();
    private Set<ReceiveChannel> channelsInSelectionAndEmpty = new HashSet<>();

    private Set<ReceiveChannel> channelsInSelectionAndClosed = new HashSet<>();
    public <E> void addChannel(ReceiveChannel<E> receiveChannel) {
        channelsInSelection.add(receiveChannel);
    }

    public boolean isEmpty() {
        return channelsInSelection.isEmpty();
    }

    public <E> void channelClosed(ReceiveChannel receiveChannel) {
        channelsInSelectionAndClosed.add(receiveChannel);
    }

    public <E> void channelEmpty(ReceiveChannel<E> receiveChannel) {
        channelsInSelectionAndEmpty.add(receiveChannel);
    }

    public boolean isAllChannelsClosed() {
        return channelsInSelection.size() == channelsInSelectionAndClosed.size();
    }

    public void done() {
        // selection finished execution, free all resources to help the GC
        channelsInSelection = new HashSet<>();
        channelsInSelectionAndEmpty = new HashSet<>();
        channelsInSelectionAndClosed = new HashSet<>();
    }

    public boolean isAllChannelsEmpty() {
        return channelsInSelection.size() == channelsInSelectionAndEmpty.size();
    }

}
