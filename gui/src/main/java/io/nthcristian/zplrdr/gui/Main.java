package io.nthcristian.zplrdr.gui;

import javax.swing.SwingUtilities;

/**
 * Entry point for the zplrdr Swing GUI.
 *
 * <p>Sets {@code java.awt.headless} to {@code false} and enables font
 * antialiasing <em>before</em> AWT initializes. The GUI is then started
 * on the Event Dispatch Thread via {@link SwingUtilities#invokeLater}.</p>
 */
public final class Main {

    private Main() {
    }

    /**
     * Launches the zplrdr GUI application.
     *
     * <p>Properties <em>must</em> be set before any AWT class loads.
     * The TSPL-based {@link io.nthcristian.prt.PrinterService} does not
     * touch AWT at all, but setting headless=false is still good practice
     * for Swing applications.</p>
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
