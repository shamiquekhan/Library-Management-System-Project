package lms.exception;

public class MemberNotFoundException extends LMSException {
    public MemberNotFoundException(String message) {
        super(message);
    }
    public MemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}