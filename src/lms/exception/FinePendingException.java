package lms.exception;

/**
 * Thrown when a member has an unpaid fine that must be cleared before
 * issuing or renewing books.
 */
public class FinePendingException extends LMSException {
    public FinePendingException(String message) {
        super(message);
    }
}
