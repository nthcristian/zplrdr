package io.nthcristian.zplrdr.error;

public class FileValidationException extends Exception {

    public FileValidationException(String message) {
        super(message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
