package com.laptrinhjavaweb.orm.exception;

public class TormException extends RuntimeException {
    public TormException(String message) {
        super(message);
    }

    public TormException(Throwable cause) {
        super(cause);
    }

    public TormException(String message, Throwable cause) {
        super(message, cause);
    }
}