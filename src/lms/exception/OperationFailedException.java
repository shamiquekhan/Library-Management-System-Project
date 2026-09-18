package lms.exception;

public class OperationFailedException extends LMSException {
    public OperationFailedException(String message) {
        super(message);
    }
    public OperationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}