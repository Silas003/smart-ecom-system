package com.ecom.exceptions;

public class AuthenticationException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String reasonCode;

    public AuthenticationException(String message) {
        this(message, null, null);
    }

    public AuthenticationException(String message, String reasonCode) {
        this(message, reasonCode, null);
    }

    public AuthenticationException(String message, String reasonCode, Throwable cause) {
        super(message, cause);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}
