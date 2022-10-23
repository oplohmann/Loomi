package org.objectscape.loomi;

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

}
