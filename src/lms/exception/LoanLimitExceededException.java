package lms.exception;

public class LoanLimitExceededException extends LMSException {
    public LoanLimitExceededException(String message) {
        super(message);
    }
    public LoanLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}