package org.objectscape.loomi;

public class ChannelInterruptedException extends RuntimeException {

    public ChannelInterruptedException() {
    }

    public ChannelInterruptedException(String message) {
        super(message);
    }

    public ChannelInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelInterruptedException(Throwable cause) {
        super(cause);
    }

    public ChannelInterruptedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
