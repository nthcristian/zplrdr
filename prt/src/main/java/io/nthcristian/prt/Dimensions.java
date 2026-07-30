package io.nthcristian.prt;

import io.nthcristian.zplrdr.preset.Preset;

/**
 * Physical label dimensions used for TSPL rendering.
 *
 * @param widthMm   label width in millimetres
 * @param heightMm  label height in millimetres
 * @param dpi       printer resolution in dots per inch
 */
public record Dimensions(int widthMm, int heightMm, float dpi) {

    /**
     * Builds dimensions from a preset.
     *
     * <p>Preset fields are in inches ({@code width}, {@code height}) and
     * dots-per-mm ({@code dpmm}). This method converts both to the units
     * expected by TSPL commands (mm for SIZE, dots for the bitmap).</p>
     *
     * @param preset the preset containing {@code width}, {@code height}, and {@code dpmm} fields
     * @return dimensions in mm and dpi
     */
    public static Dimensions fromPreset(Preset preset) {
        float widthIn = Float.parseFloat(preset.getProperty("width"));
        float heightIn = Float.parseFloat(preset.getProperty("height"));
        float dpmm = Float.parseFloat(
                preset.getProperty("dpmm").replace("dpmm", ""));
        float dpi = dpmm * 25.4f;
        int wMm = Math.round(widthIn * 25.4f);
        int hMm = Math.round(heightIn * 25.4f);
        return new Dimensions(wMm, hMm, dpi);
    }

    /**
     * Label width in dots (pixels) at the printer's resolution.
     */
    public int widthDots() {
        return Math.round(widthMm * (dpi / 25.4f));
    }

    /**
     * Label height in dots (pixels) at the printer's resolution.
     */
    public int heightDots() {
        return Math.round(heightMm * (dpi / 25.4f));
    }
}
