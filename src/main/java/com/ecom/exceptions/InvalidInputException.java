package com.ecom.exceptions;

public class InvalidInputException extends ValidationException {
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }

}
