package io.nthcristian.prt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.nthcristian.prt.PdfBitmapRenderer.BitmapImage;

/**
 * Builds a single TSPL print job for one label.
 *
 * <p>Generates the TSPL command sequence:</p>
 * <pre>
 *   SIZE w mm,h mm
 *   CLS
 *   BITMAP 0,0,widthBytes,height,0,&lt;data&gt;
 *   PRINT 1,1
 * </pre>
 *
 * <p>The bitmap is positioned at the top-left origin (0, 0).
 * The PDF from Labelary already matches the label dimensions exactly,
 * so no centering offset is needed.</p>
 */
final class TsplLabel {

    private TsplLabel() {
    }

    /**
     * Generates the raw TSPL byte sequence for one label.
     *
     * @param dims   label dimensions
     * @param bitmap rendered monochrome bitmap of the label content
     * @return raw TSPL command bytes ready to send to the printer
     */
    static byte[] generate(Dimensions dims, BitmapImage bitmap) throws IOException {
        int rowBytes = (bitmap.width() + 7) / 8;

        var buf = new ByteArrayOutputStream();

        // Leading whitespace clears any leftover state on the printer
        buf.write("\r\n\r\n".getBytes(StandardCharsets.US_ASCII));

        // SIZE command
        String sizeCmd = "SIZE %d mm,%d mm\r\n".formatted(dims.widthMm(), dims.heightMm());
        buf.write(sizeCmd.getBytes(StandardCharsets.US_ASCII));

        // CLS — clear image buffer
        buf.write("CLS\r\n".getBytes(StandardCharsets.US_ASCII));

        // BITMAP at origin (0,0) — full-bleed, no centering
        String bmpHeader = "BITMAP 0,0,%d,%d,0,"
                .formatted(rowBytes, bitmap.height());
        buf.write(bmpHeader.getBytes(StandardCharsets.US_ASCII));

        // Bitmap data (raw bytes)
        buf.write(bitmap.data());

        // PRINT command — 1 copy
        buf.write("\r\nPRINT 1,1\r\n".getBytes(StandardCharsets.US_ASCII));

        return buf.toByteArray();
    }
}
