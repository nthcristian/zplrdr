package io.nthcristian.prt.error;

public class PrinterServiceException extends Exception {

    public PrinterServiceException(String message) {
        super(message);
    }

    public PrinterServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
