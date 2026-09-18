package lms.exception;

public class BookNotFoundException extends LMSException {
    public BookNotFoundException(String message) {
        super(message);
    }
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}