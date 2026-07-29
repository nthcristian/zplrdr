package io.nthcristian.prt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.nthcristian.prt.error.PrinterServiceException;
import io.nthcristian.zplrdr.document.PdfDocument;

@DisplayName("PrinterService")
class PrinterServiceTest {

    /** Default Labelary label size (inches): ~100x150mm shipping label for MDK-006. */
    private static final float LABEL_WIDTH_IN = 3.9f;
    private static final float LABEL_HEIGHT_IN = 5.9f;

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
    @DisplayName("listPrinters")
    class ListPrintersTests {

        @Test
        @DisplayName("should return a non-null array")
        void shouldReturnNonNullArray() {
            String[] printers = assertDoesNotThrow(printerService::listPrinters);

            assertNotNull(printers);
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
                    () -> printerService.print(document));

            assertTrue(exception.getMessage().contains("empty"));
        }

        @Test
        @DisplayName("should reject null PDF document")
        void shouldRejectNullPdfDocument() {
            PrinterServiceException exception = assertThrows(PrinterServiceException.class,
                    () -> printerService.print(null));

            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("should reject unknown printer name")
        void shouldRejectUnknownPrinterName() {
            PdfDocument document = new PdfDocument(MINIMAL_PDF);

            PrinterServiceException exception = assertThrows(PrinterServiceException.class,
                    () -> printerService.print(document, "nonexistent-printer-xyz-12345"));

            assertTrue(exception.getMessage().contains("Printer not found"));
        }

        @Test
        @DisplayName("should reject blank printer name")
        void shouldRejectBlankPrinterName() {
            PdfDocument document = new PdfDocument(MINIMAL_PDF);

            PrinterServiceException exception = assertThrows(PrinterServiceException.class,
                    () -> printerService.print(document, "  "));

            assertTrue(exception.getMessage().contains("blank"));
        }

        @Test
        @DisplayName("should reject invalid PDF data when a printer is available")
        void shouldRejectInvalidPdfDataWhenPrinterAvailable() {
            String[] printers = printerService.listPrinters();
            if (printers.length == 0) {
                PrinterServiceException exception = assertThrows(PrinterServiceException.class,
                        () -> printerService.print(new PdfDocument(MINIMAL_PDF)));
                assertTrue(exception.getMessage().contains("No default print service"));
                return;
            }

            PdfDocument document = new PdfDocument("not-a-pdf".getBytes(StandardCharsets.UTF_8));

            PrinterServiceException exception = assertThrows(PrinterServiceException.class,
                    () -> printerService.print(document, printers[0]));

            assertTrue(exception.getMessage().contains("Failed to load PDF"));
        }
    }

    @Nested
    @DisplayName("thermal label layout")
    class ThermalLabelLayoutTests {

        @Test
        @DisplayName("should size page format to label MediaBox with full-bleed imageable area")
        void shouldSizePageFormatToLabelMediaBox() throws IOException {
            try (PDDocument document = labelDocument(LABEL_WIDTH_IN, LABEL_HEIGHT_IN)) {
                Pageable pageable = LabelPrintLayout.createPageable(document);
                PageFormat format = pageable.getPageFormat(0);

                assertEquals(LABEL_WIDTH_IN * 72, format.getWidth(), 0.5);
                assertEquals(LABEL_HEIGHT_IN * 72, format.getHeight(), 0.5);
                assertTrue(LabelPrintLayout.isFullBleed(format),
                        "imageable area must cover the full label to avoid clipping");
            }
        }

        @Test
        @DisplayName("should request monochrome full-bleed media matching the label")
        void shouldRequestMonochromeFullBleedMedia() throws IOException {
            try (PDDocument document = labelDocument(LABEL_WIDTH_IN, LABEL_HEIGHT_IN)) {
                PrintRequestAttributeSet attrs = LabelPrintLayout.createAttributes(document);

                MediaPrintableArea area = (MediaPrintableArea) attrs.get(MediaPrintableArea.class);
                assertNotNull(area);
                assertEquals(0f, area.getX(MediaPrintableArea.INCH), 0.001f);
                assertEquals(0f, area.getY(MediaPrintableArea.INCH), 0.001f);
                assertEquals(LABEL_WIDTH_IN, area.getWidth(MediaPrintableArea.INCH), 0.01f);
                assertEquals(LABEL_HEIGHT_IN, area.getHeight(MediaPrintableArea.INCH), 0.01f);
                assertEquals(Chromaticity.MONOCHROME, attrs.get(Chromaticity.class));
                assertEquals(OrientationRequested.PORTRAIT, attrs.get(OrientationRequested.class));
            }
        }

        @Test
        @DisplayName("should use landscape orientation when label is wider than tall")
        void shouldUseLandscapeWhenWiderThanTall() throws IOException {
            try (PDDocument document = labelDocument(5.9f, 3.9f)) {
                PrintRequestAttributeSet attrs = LabelPrintLayout.createAttributes(document);

                assertEquals(OrientationRequested.LANDSCAPE, attrs.get(OrientationRequested.class));
            }
        }

        private PDDocument labelDocument(float widthInches, float heightInches) throws IOException {
            PDDocument document = new PDDocument();
            document.addPage(new PDPage(new PDRectangle(widthInches * 72f, heightInches * 72f)));
            // Ensure the document is a valid writable PDF (round-trip sanity).
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return document;
        }
    }

}
