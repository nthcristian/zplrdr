package io.nthcristian.zplrdr.error;

public class FieldValidationException extends Exception {

    public FieldValidationException(String message) {
        super(message);
    }

    public FieldValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
