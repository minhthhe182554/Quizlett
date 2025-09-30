package com.hminq.quizlett.utils;

import com.hminq.quizlett.exceptions.ValidationException;

import java.util.regex.Pattern;

public class InputValidator {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Common validate methods
    private static void validateEmail(String email) throws ValidationException {
        if (email == null || email.isEmpty()) {
            throw new ValidationException(ValidationException.Field.EMAIL, "Email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(ValidationException.Field.EMAIL, "Email format is incorrect.");
        }
    }

    private static void validatePassword(String password) throws ValidationException {
        if (password == null || password.isEmpty()) {
            throw new ValidationException(ValidationException.Field.PASSWORD, "Password cannot be empty.");
        }
        if (password.length() < 8 || password.length() > 50) {
            throw new ValidationException(ValidationException.Field.PASSWORD, "Password length must be in range 8-50.");
        }
    }

    private static void validateFullname(String fullname) throws ValidationException {
        if (fullname == null || fullname.isEmpty()) {
            throw new ValidationException(ValidationException.Field.FULLNAME, "Fullname cannot be empty.");
        }
        if (fullname.length() < 5 || fullname.length() > 50) {
            throw new ValidationException(ValidationException.Field.FULLNAME, "Fullname length must be in range 5-50.");
        }
    }

    // Public methods
    public static void validateInput(String email) throws ValidationException {
        validateEmail(email);
    }

    public static void validateInput(String email, String password) throws ValidationException {
        validateEmail(email);
        validatePassword(password);
    }

    public static void validateInput(String email, String password, String fullname) throws ValidationException {
        validateEmail(email);
        validatePassword(password);
        validateFullname(fullname);
    }
}
