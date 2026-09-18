package lms.exception;

public class HoldNotAllowedException extends LMSException {
    public HoldNotAllowedException(String message) {
        super(message);
    }
    public HoldNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}