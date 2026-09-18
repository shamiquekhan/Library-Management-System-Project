package lms.exception;

public class LMSException extends Exception {
    public LMSException(String message) {
        super(message);
    }

    public LMSException(String message, Throwable cause) {
        super(message, cause);
    }
}