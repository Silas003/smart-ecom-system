package com.ecom.utils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

import com.ecom.exceptions.InvalidInputException;
import com.ecom.exceptions.ValidationException;

public final class ValidationUtils {
    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern LETTER = Pattern.compile(".*[A-Za-z].*");

    private ValidationUtils() {}

    public static String trimToNull(String input) {
        if (input == null) return null;
        String t = input.trim();
        return t.isEmpty() ? null : t;
    }

    public static String requireNonEmpty(String input, String fieldName) throws InvalidInputException {
        String t = trimToNull(input);
        if (t == null) throw new InvalidInputException(fieldName + " is required", fieldName);
        return t;
    }

    public static void requireEmail(String email, String fieldName) throws InvalidInputException {
        String t = requireNonEmpty(email, fieldName);
        if (!EMAIL.matcher(t).matches()) {
            throw new InvalidInputException("Invalid email format", fieldName, email);
        }
    }

    public static void requireStrongPassword(String password, String fieldName) throws ValidationException {
        String t = requireNonEmpty(password, fieldName);
        if (t.length() < 8 || !DIGIT.matcher(t).matches() || !LETTER.matcher(t).matches()) {
            throw new ValidationException("Password must be at least 8 characters and include letters and numbers", fieldName, password);
        }
    }

    public static void requirePositive(BigDecimal value, String fieldName) throws ValidationException {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName + " must be zero or positive", fieldName, value);
        }
    }

    public static void requireNonNegativeInt(int value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(fieldName + " must be non-negative", fieldName, value);
        }
    }

    public static void requireInRange(int value, int min, int max, String fieldName) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(String.format("%s must be between %d and %d", fieldName, min, max), fieldName, value);
        }
    }
}

