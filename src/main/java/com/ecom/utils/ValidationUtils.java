package com.ecom.utils;

import com.ecom.exceptions.ValidationException;

/**
 * Utility class for reusable validation methods.
 */
public class ValidationUtils {

    public static void validateEmail(String email) throws ValidationException {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException("Invalid email format.");
        }
    }

    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
    }

    public static void validateNotEmpty(String field, String fieldName) throws ValidationException {
        if (field == null || field.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validatePositive(int number, String fieldName) throws ValidationException {
        if (number <= 0) {
            throw new ValidationException(fieldName + " must be a positive number.");
        }
    }

    public static void validateRegex(String value, String regex, String fieldName) throws ValidationException {
        if (value == null || !value.matches(regex)) {
            throw new ValidationException(fieldName + " does not match the required format.");
        }
    }

    public static void requireNonEmpty(String email, String email1) throws  ValidationException{
    }

    public static void requireEmail(String email, String email1) throws ValidationException{
    }

    public static void requireStrongPassword(String password, String password1) {
    }

    public static String trimToNull(String email) {
        return email.trim();
    }
}
