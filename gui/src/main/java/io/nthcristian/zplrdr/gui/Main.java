package io.nthcristian.zplrdr.gui;

import javax.swing.SwingUtilities;

/**
 * Entry point for the zplrdr Swing GUI.
 *
 * <p>Sets {@code java.awt.headless} to {@code false} <em>before</em> any
 * AWT or PrinterService class is loaded, ensuring the desktop window can
 * be created. The GUI is then started on the Event Dispatch Thread via
 * {@link SwingUtilities#invokeLater}.</p>
 */
public final class Main {

    private Main() {
    }

    /**
     * Launches the zplrdr GUI application.
     *
     * <p>The headless property <em>must</em> be set as the very first
     * statement. {@link io.nthcristian.prt.PrinterService} has a static
     * initializer that only sets headless when no explicit value exists,
     * so this override takes precedence and allows Swing windows to be
     * created.</p>
     *
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        // Must be set before any AWT/Swing class or PrinterService loads.
        // PrinterService's static block only sets headless when no explicit
        // value has been set, so this override takes precedence.
        System.setProperty("java.awt.headless", "false");

        // Enable LCD subpixel font antialiasing for crisp text rendering.
        // Also valid: "on" (grayscale), "gasp" (font's built-in hinting).
        System.setProperty("awt.useSystemAAFontSettings", "on");

        SwingUtilities.invokeLater(() -> {
            var app = new GuiApplication();
            app.setVisible(true);
        });
    }
}
