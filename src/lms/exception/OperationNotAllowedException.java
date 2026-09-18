package lms.exception;

public class OperationNotAllowedException extends LMSException {
    public OperationNotAllowedException(String message) {
        super(message);
    }
    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}