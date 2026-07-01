package net.javaguides.sms_backend.exception;

public class NameAggregationException extends RuntimeException {
    public NameAggregationException(String message) {
        super(message);
    }

    public NameAggregationException(String message, Throwable cause) {
        super(message, cause);
    }
}
