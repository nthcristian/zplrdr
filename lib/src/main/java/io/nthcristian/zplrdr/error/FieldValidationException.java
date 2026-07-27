package io.nthcristian.zplrdr.error;

public class FieldValidationException extends Exception {
    public FieldValidationException() {
        super("One or more fields could not be validated.");
    }

}
