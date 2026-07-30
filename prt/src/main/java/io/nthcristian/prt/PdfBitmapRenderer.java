package io.nthcristian.prt;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Renders a PDF page to a 1-bit monochrome bitmap suitable for TSPL
 * {@code BITMAP} commands.
 *
 * <p>Works like the Python {@code pdf2tspl.py} pipeline:</p>
 * <ol>
 *   <li>Render the PDF page at 2× the label's native DPI via PDFBox
 *       (supersampling to capture thin lines and barcodes).</li>
 *   <li>Scale down to the exact label dot dimensions.</li>
 *   <li>Threshold to monochrome (1 bit per pixel).</li>
 *   <li>Pack 8 pixels into each byte (MSB = leftmost pixel).</li>
 * </ol>
 */
final class PdfBitmapRenderer {

    /** Render at 2× DPI for supersampling anti-aliasing of thin lines. */
    private static final float SUPERSAMPLE = 2f;

    private PdfBitmapRenderer() {
    }

    /**
     * A monochrome bitmap image ready for a TSPL {@code BITMAP} command.
     *
     * @param width  image width in pixels
     * @param height image height in pixels
     * @param data   packed bitmap bytes, row-major, 8 pixels per byte (MSB first)
     */
    record BitmapImage(int width, int height, byte[] data) {
    }

    /**
     * Renders a single PDF page to a monochrome bitmap at the label's
     * native dot dimensions.
     *
     * @param document  the loaded PDF document
     * @param pageIndex zero-based page index to render
     * @param dims      target label dimensions
     * @return the rendered monochrome bitmap (width == dims.widthDots, height == dims.heightDots)
     */
    static BitmapImage renderPage(PDDocument document, int pageIndex,
                                  Dimensions dims) throws java.io.IOException {
        int labelW = dims.widthDots();
        int labelH = dims.heightDots();

        PDFRenderer renderer = new PDFRenderer(document);

        // Render at 2× DPI for supersampling (captures thin barcode lines)
        float scale = dims.dpi() / 72f;
        BufferedImage image = renderer.renderImage(pageIndex, scale * SUPERSAMPLE);

        int rawW = image.getWidth();
        int rawH = image.getHeight();

        // Scale to fit label width, preserving aspect ratio
        // (same logic as pdf2tspl.py convert_pdf_scaled)
        float aspect = (float) rawW / rawH;
        float labelAspect = (float) labelW / labelH;
        int fitW, fitH;
        if (aspect < labelAspect) {
            fitH = Math.round(labelH * SUPERSAMPLE);
            fitW = Math.round(fitH * aspect);
        } else {
            fitW = Math.round(labelW * SUPERSAMPLE);
            fitH = Math.round(fitW / aspect);
        }

        // Downscale to label dimensions
        if (rawW != fitW || rawH != fitH) {
            image = scaleImage(image, fitW, fitH);
        }
        image = scaleImage(image, labelW, labelH);

        // Convert to 1-bit monochrome
        byte[] packed = toMonochrome(image);
        return new BitmapImage(labelW, labelH, packed);
    }

    /**
     * Scales a BufferedImage to the given dimensions using bilinear interpolation.
     */
    private static BufferedImage scaleImage(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        return dst;
    }

    /**
     * Converts a grayscale image to packed monochrome bytes.
     * Dark pixels (luminance &lt; 160) become black, others white.
     * Each row is packed into {@code (width+7)/8} bytes, MSB first.
     * After packing, every byte is XORed with {@code 0xFF} to match
     * the bit order used by TSPL (same as pdftoppm + x^0xff in pdf2tspl.py).
     */
    static byte[] toMonochrome(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int rowBytes = (w + 7) / 8;
        byte[] out = new byte[rowBytes * h];

        for (int y = 0; y < h; y++) {
            int rowOffset = y * rowBytes;
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                // ITU-R BT.601 luminance
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                if (lum < 160) {
                    int byteIdx = rowOffset + (x / 8);
                    int bitIdx = 7 - (x % 8);  // MSB = leftmost
                    out[byteIdx] |= (byte) (1 << bitIdx);
                }
            }
        }
        // Invert every byte to match TSPL bit order (pdf2tspl.py x ^ 0xff)
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (out[i] ^ 0xFF);
        }
        return out;
    }
}
