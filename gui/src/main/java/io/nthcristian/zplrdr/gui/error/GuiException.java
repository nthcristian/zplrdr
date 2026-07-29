package io.nthcristian.zplrdr.gui.error;

/**
 * Checked exception for errors originating in the Swing GUI layer.
 *
 * <p>Wraps backend exceptions ({@link io.nthcristian.zplrdr.error.ZplConverterException},
 * {@link io.nthcristian.prt.error.PrinterServiceException}, etc.) with
 * user-facing context messages, following the same checked-exception
 * pattern used throughout the project.</p>
 */
public class GuiException extends Exception {

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message describes what went wrong in the GUI layer
     */
    public GuiException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and root cause.
     *
     * @param message describes what went wrong
     * @param cause   the underlying exception that triggered this error
     */
    public GuiException(String message, Throwable cause) {
        super(message, cause);
    }
}
