package io.nthcristian.prt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.nthcristian.prt.PdfBitmapRenderer.BitmapImage;
import io.nthcristian.prt.error.PrinterServiceException;
import io.nthcristian.zplrdr.document.PdfDocument;

@DisplayName("PrinterService")
class PrinterServiceTest {

    /** Label size: ~100×150 mm at 203 DPI (8 dpmm). */
    private static final Dimensions LABEL_DIMS = new Dimensions(102, 152, 203.2f);

    private static final byte[] MINIMAL_PDF = """
            %PDF-1.1
            1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj
            2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj
            3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>endobj
            xref
            0 4
            0000000000 65535 f\s
            0000000009 00000 n\s
            0000000058 00000 n\s
            0000000115 00000 n\s
            trailer<< /Size 4 /Root 1 0 R >>
            startxref
            190
            %%EOF
            """.getBytes(StandardCharsets.US_ASCII);

    private final PrinterService printerService = new PrinterService();

    @Nested
    @DisplayName("listDevices")
    class ListDevicesTests {

        @Test
        @DisplayName("should return a non-null array")
        void shouldReturnNonNullArray() {
            String[] devices = assertDoesNotThrow(PrinterService::listDevices);
            assertNotNull(devices);
        }
    }

    @Nested
    @DisplayName("print validation")
    class PrintValidationTests {

        @Test
        @DisplayName("should reject empty PDF data")
        void shouldRejectEmptyPdfData() {
            PdfDocument document = new PdfDocument(new byte[0]);

            PrinterServiceException exception = assertThrows(PrinterServiceException.class,
                    () -> printerService.print(document, "/dev/null", LABEL_DIMS));

            assertTrue(exception.getMessage().contains("empty"));
        }

        @Test
        @DisplayName("should reject null PDF document")
        void shouldRejectNullPdfDocument() {
            PrinterServiceException exception = assertThrows(PrinterServiceException.class,
                    () -> printerService.print(null, "/dev/null", LABEL_DIMS));

            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("should reject invalid PDF data")
        void shouldRejectInvalidPdfData() {
            PdfDocument document = new PdfDocument("not-a-pdf".getBytes(StandardCharsets.UTF_8));

            PrinterServiceException exception = assertThrows(PrinterServiceException.class,
                    () -> printerService.print(document, "/dev/null", LABEL_DIMS));

            assertTrue(exception.getMessage().contains("Failed to load PDF"));
        }

        @Test
        @DisplayName("should reject null dimensions")
        void shouldRejectNullDimensions() {
            PdfDocument document = new PdfDocument(MINIMAL_PDF);

            assertThrows(NullPointerException.class,
                    () -> printerService.print(document, "/dev/null", null));
        }
    }

    @Nested
    @DisplayName("TSPL bitmap rendering")
    class TsplRenderingTests {

        @Test
        @DisplayName("should render PDF page to monochrome bitmap at label dimensions")
        void shouldRenderPageAtLabelDimensions() throws IOException {
            try (PDDocument document = labelDocument(3.9f, 5.9f)) {
                BitmapImage bitmap = PdfBitmapRenderer.renderPage(document, 0, LABEL_DIMS);

                assertNotNull(bitmap);
                assertEquals(LABEL_DIMS.widthDots(), bitmap.width(),
                        "bitmap width must equal label width");
                assertEquals(LABEL_DIMS.heightDots(), bitmap.height(),
                        "bitmap height must equal label height");
                assertEquals((bitmap.width() + 7) / 8 * bitmap.height(), bitmap.data().length,
                        "packed data size must match row_bytes × height");
            }
        }

        @Test
        @DisplayName("should generate valid TSPL command sequence")
        void shouldGenerateValidTsplCommands() throws IOException {
            try (PDDocument document = labelDocument(4f, 6f)) {
                BitmapImage bitmap = PdfBitmapRenderer.renderPage(document, 0, LABEL_DIMS);
                byte[] tspl = TsplLabel.generate(LABEL_DIMS, bitmap);

                String cmd = new String(tspl, StandardCharsets.US_ASCII);
                assertTrue(cmd.contains("SIZE"), "must contain SIZE command");
                assertTrue(cmd.contains("CLS"), "must contain CLS command");
                assertTrue(cmd.contains("BITMAP"), "must contain BITMAP command");
                assertTrue(cmd.contains("PRINT 1,1"), "must contain PRINT command");
            }
        }

        @Test
        @DisplayName("should handle multi-page PDF producing one TSPL job per page")
        void shouldHandleMultiPagePdf() throws IOException {
            try (PDDocument document = new PDDocument()) {
                document.addPage(new PDPage(new PDRectangle(3.9f * 72f, 5.9f * 72f)));
                document.addPage(new PDPage(new PDRectangle(3.9f * 72f, 5.9f * 72f)));

                // Serialize and re-load as PdfDocument
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.save(out);
                PdfDocument pdfDoc = new PdfDocument(out.toByteArray());

                // printAll should succeed when writing to /dev/null
                // (this exercises the full pipeline without needing a real printer)
                assertDoesNotThrow(() ->
                        printerService.printAll(new PdfDocument[]{pdfDoc},
                                "/dev/null", LABEL_DIMS));
            }
        }
    }

    @Nested
    @DisplayName("Dimensions conversion")
    class DimensionsTests {

        @Test
        @DisplayName("should compute dots from mm and dpi")
        void shouldComputeDots() {
            // 102 mm at 203.2 DPI ≈ 816 dots
            Dimensions dims = new Dimensions(102, 152, 203.2f);
            assertTrue(dims.widthDots() > 800 && dims.widthDots() < 820);
            assertTrue(dims.heightDots() > 1200 && dims.heightDots() < 1230);
        }
    }

    private static PDDocument labelDocument(float widthInches, float heightInches)
            throws IOException {
        PDDocument document = new PDDocument();
        document.addPage(new PDPage(new PDRectangle(widthInches * 72f, heightInches * 72f)));
        // Round-trip through save to produce a valid document
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        return Loader.loadPDF(out.toByteArray());
    }
}
