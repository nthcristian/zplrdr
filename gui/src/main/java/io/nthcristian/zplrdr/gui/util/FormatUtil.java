package io.nthcristian.zplrdr.gui.util;

/**
 * Shared formatting utilities for the GUI.
 */
public final class FormatUtil {

    private FormatUtil() {
    }

    /**
     * Formats a byte count into a human-readable string (B, KB, MB).
     *
     * @param bytes the number of bytes
     * @return a formatted string like "1.5 KB"
     */
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format("%.1f MB", mb);
    }
}
