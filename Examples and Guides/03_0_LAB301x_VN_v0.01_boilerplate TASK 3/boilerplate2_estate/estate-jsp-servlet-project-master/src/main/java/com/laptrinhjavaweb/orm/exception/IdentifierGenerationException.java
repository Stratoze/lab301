package com.laptrinhjavaweb.orm.exception;

public class IdentifierGenerationException extends TormException {
    public IdentifierGenerationException(String message) {
        super(message);
    }

    public IdentifierGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
