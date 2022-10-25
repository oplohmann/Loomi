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
        var proceed = true;
        var firstIteration = true;

        while (proceed) {
            if(!firstIteration && selection.isAllChannelsClosed()) {
                break;
            }
            if(!firstIteration && selection.isAllChannelsEmpty()) {
                // Deadlock: throw exception
                break;
            }
            action.accept(selection);
            firstIteration = false;
            if(selection.isEmpty()) {
                return;
            }
        }

        selection.done();
    }
}
