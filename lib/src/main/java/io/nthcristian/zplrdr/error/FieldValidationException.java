package io.nthcristian.zplrdr.error;

public class FieldValidationException extends Exception {
    private static final String defaultMessage = "One or more fields could not be validated.";

    public FieldValidationException() {
        super(defaultMessage);
    }

    public FieldValidationException(Throwable cause) {
        super(defaultMessage, cause);
    }

}
