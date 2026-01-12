package com.ecom.exceptions;

public class InvalidInputException extends ValidationException {
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, String fieldName) {
        super(message, fieldName);
    }

    public InvalidInputException(String message, String fieldName, Object invalidValue) {
        super(message, fieldName, invalidValue);
    }

    public InvalidInputException(String message, String fieldName, Object invalidValue, Throwable cause) {
        super(message, fieldName, invalidValue, cause);
    }
}
