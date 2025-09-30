package com.hminq.quizlett.exceptions;

public class ValidationException extends Exception{
    public enum Field {
        EMAIL,
        PASSWORD,
        FULLNAME
    }

    private final Field field;

    public ValidationException(Field field, String message) {
        super(message);
        this.field = field;
    }

    public Field getField() {
        return field;
    }
}
