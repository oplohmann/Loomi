package org.objectscape.loomi;

import java.util.HashSet;
import java.util.Set;

public class ChannelSelection {

    private Set<ReceiveChannel> channelsInSelection = new HashSet<>();
    private Set<ReceiveChannel> channelsInSelectionAndEmpty = new HashSet<>();
    private Set<ReceiveChannel> channelsInSelectionAndClosed = new HashSet<>();

    private boolean firstIteration = true;
    private boolean done = false;

    private boolean defaultExists = false;
    public <E> void addChannel(ReceiveChannel<E> receiveChannel) {
        if(firstIteration) {
            channelsInSelection.add(receiveChannel);
        }
    }

    public boolean isNoChannelsDefined() {
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

    protected void clear() {
        // selection finished execution, free all resources to help the GC reclaim unreferenced channels
        channelsInSelection = new HashSet<>();
        channelsInSelectionAndEmpty = new HashSet<>();
        channelsInSelectionAndClosed = new HashSet<>();
    }

    public boolean isAllChannelsEmpty() {
        return channelsInSelection.size() == channelsInSelectionAndEmpty.size();
    }

    public boolean isFirstIteration() {
        return firstIteration;
    }

    public void setFirstIteration(boolean firstIteration) {
        this.firstIteration = firstIteration;
    }

    public void done() {
        done = true;
    }

    protected boolean isDone() {
        return done;
    }

    public void onDefault(Runnable runnable) {
        defaultExists = true;
        if(isDone()) {
            return;
        }
        if(isAllChannelsEmpty()) {
            runnable.run();
        }
    }

    protected boolean isDefaultExists() {
        return defaultExists;
    }

}
