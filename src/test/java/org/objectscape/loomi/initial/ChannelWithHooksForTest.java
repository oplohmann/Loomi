package org.objectscape.loomi.initial;

import org.objectscape.loomi.initial.Channel;

public class ChannelWithHooksForTest<E> extends Channel<E> {

    public ChannelWithHooksForTest() {
    }

    /**
     * Hook for test only. The user is not supposed to be able to call in.isClosed() as in the code below in line 1,
     * because:
     *
     * 1. This could result in a ChannelClosedElement to remain put in the channel as line 3 in the code below
     * would not be executed anymore after in.isClosed in line 1 returned true.
     *
     * 2. The number of ChannelClosedElement in the channel is not arbitrary, but is counted so that there is
     * one ChannelClosedElement in the channel for every waiting receiver. The user not being able to call
     * in.Closed() as in line 1 below forces the user to call in.receive() as in line 3.
     *
     * 3. Some ChannelClosedElement remaining put in the channel would prevent the channel from being garbage
     * collected as ChannelClosedElement for efficiency reasons is a singleton object.
     *
     * 4. Some channel might have been closed, but still contains ChannelElements to be received, which then
     * would never be processed.
     *
     * 5. Some race condition would be possible so that in.receive() as in line 3 below would block forever and this
     * way used up some virtual thread forever.
     *
     * 6. If in line 2 below it would say in.isEmptyAndClosed() instead of in.isClosed() there would
     * still race conditions be possible that result in in.receive() in line 3 to block forever.
     *
     * private void foo(ReceiveChannel<Integer> in) {       // 1
     *         while(in.isClosed) {                         // 2 NO!
     *             var channelElement = in.receive();       // 3
     *             out.send(channelElement.get() * 2);      // 4
     *         }
     *     }
     *
     * Instead the user is supposed to take this approach:
     *
     * private void foo(ReceiveChannel<Integer> in) {
     *          while(true) {
     *              var channelElement = in.receive();
     *              if(channelElement.isChannelEmptyAndClosed()) {  // YES!
     *                  return;
     *              }
     *          }
     *      }
     *
     * @return
     */
    public boolean isClosed() {
        closedLock.readLock().lock();

        try {
            return closed;
        } finally {
            closedLock.readLock().unlock();
        }
    }

    /**
     * Hook for test only. There is no point in the user asking whether some channel is empty or not. This is
     * against the conceptual approach how a channel is supposed to be used: only send and receive and ask
     * ChannelElement.isChannelClosed() on a received ChannelElement is provided and nothing more. This is
     * what makes the concept of using channels so simple and free of special cases.
     * @return
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    protected void waitingConsumerCount(int waitingConsumerCount) {
        super.waitingConsumerCount(waitingConsumerCount);
    }

}
