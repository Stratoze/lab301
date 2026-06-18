package com.laptrinhjavaweb.orm.exception;

public class EntityMappingException extends TormException {
    public EntityMappingException(String message) {
        super(message);
    }

    public EntityMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
