package lms.exception;

public class DuplicateEntryException extends LMSException {
    public DuplicateEntryException(String message) {
        super(message);
    }
    public DuplicateEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}