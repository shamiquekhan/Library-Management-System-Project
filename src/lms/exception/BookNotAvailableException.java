package lms.exception;

public class BookNotAvailableException extends LMSException {
    public BookNotAvailableException(String message) {
        super(message);
    }
    public BookNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}