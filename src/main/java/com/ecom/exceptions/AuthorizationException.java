package com.ecom.exceptions;

public class AuthorizationException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String action;
    private final String requiredRole;

    public AuthorizationException(String message, String action, String requiredRole) {
        super(message);
        this.action = action;
        this.requiredRole = requiredRole;
    }

    public AuthorizationException(String message, String action, String requiredRole, Throwable cause) {
        super(message, cause);
        this.action = action;
        this.requiredRole = requiredRole;
    }

    public String getAction() {
        return action;
    }

    public String getRequiredRole() {
        return requiredRole;
    }
}
