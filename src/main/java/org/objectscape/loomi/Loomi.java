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

                if(!selection.isFirstIteration() && selection.isAllChannelsClosed()) {
                    break;
                }
                if(!selection.isFirstIteration() && !selection.isDefaultExists() && selection.isAllChannelsEmpty()) {
                    throw new DeadlockException("all channels in selection are empty: deadlock!");
                }

                action.accept(selection);
                selection.setFirstIteration(false);

                if(selection.isNoChannelsDefined()) {
                    break;
                }
            }
        } finally {
            selection.clear();
        }

    }
}
