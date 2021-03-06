package org.citopt.connde.exception;

/**
 *
 * @author rafaelkperes
 */
public class InsertFailureException extends Exception {

    public InsertFailureException() {
    }

    public InsertFailureException(String message) {
        super(message);
    }

    public InsertFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsertFailureException(Throwable cause) {
        super(cause);
    }

    public InsertFailureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
