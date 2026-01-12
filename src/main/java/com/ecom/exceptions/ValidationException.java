package com.ecom.exceptions;

public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String fieldName;
    private final Object invalidValue;

    public ValidationException(String message) {
        this(message, null, null, null);
    }

    public ValidationException(String message, String fieldName) {
        this(message, fieldName, null, null);
    }

    public ValidationException(String message, String fieldName, Object invalidValue) {
        this(message, fieldName, invalidValue, null);
    }

    public ValidationException(String message, String fieldName, Object invalidValue, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}
