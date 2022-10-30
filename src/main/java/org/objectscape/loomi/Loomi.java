package org.objectscape.loomi;

import java.util.function.Consumer;

public class Loomi {

    public static void startVirtual(Runnable runnable) {
        Thread.startVirtualThread(runnable);
    }

    /**
     * Same as calling <tt>startVirtual</tt>, <tt>goLightly</tt> indicates that JDK19 virtual threads have a reduced context compared
     * to Java kernel threads and are therefore even more lightweight threads. The method name <tt>goLightly</tt> is also
     * humorous in remembrance of the movie "Breakfast at Tiffany's" starring Audrey Hepburn as Holly Golightly.
     * @param runnable
     */
    public static void goLightly(Runnable runnable) {
        startVirtual(runnable);
    }

    public static void startKernel(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static void select(Consumer<ChannelSelection> action) {
        var selection = new ChannelSelection();

        try {
            while (!selection.isDone()) {
                if (isLeaveSelect(selection)) {
                    break;
                }
                action.accept(selection);
                handleTimeouts(selection);
                selection.setFirstIteration(false);
            }
        } finally {
            selection.clear();
        }

    }

    private static void handleTimeouts(ChannelSelection selection) {
        if(selection.isFirstIteration() || selection.isNoTimeouts()) {
            return;
        }
        // prototype only - correct synchronization implemented in a next step
        selection.getChannelsInSelection().forEach(channel -> {
            channel.addSendListener(selection);
        });
    }

    private static boolean isLeaveSelect(ChannelSelection selection) {
        if(selection.isFirstIteration()) {
            return false;
        }
        if(selection.isNoChannelsDefined() || selection.isAllChannelsClosed()) {
            return true;
        }
        if(!selection.isDefaultExists() && selection.isAllChannelsEmpty()) {
            // This is what select in Go does in this situation, keep it the same for Loomi to be predictive in its behavior
            throw new DeadlockException("all channels in selection are empty: deadlock!");
        }
        return false;
    }
}
