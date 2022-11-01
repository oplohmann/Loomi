package org.objectscape.loomi.initial;

public class DeadlockException extends RuntimeException {

    public DeadlockException() {
    }

    public DeadlockException(String message) {
        super(message);
    }

    public DeadlockException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeadlockException(Throwable cause) {
        super(cause);
    }

    public DeadlockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
