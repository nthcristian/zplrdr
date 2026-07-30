package io.nthcristian.prt;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import jakarta.validation.constraints.NotNull;
import io.nthcristian.prt.PdfBitmapRenderer.BitmapImage;
import io.nthcristian.prt.error.PrinterServiceException;
import io.nthcristian.zplrdr.document.PdfDocument;

/**
 * Prints PDF label documents on TSPL-compatible thermal printers by
 * converting each page to a monochrome bitmap and sending raw TSPL
 * commands directly to the printer device.
 *
 * <p>Bypasses the operating-system print driver entirely — no
 * {@code javax.print}, no CUPS, no spooler. Just raw bytes over TCP
 * (port 9100) or a device file ({@code /dev/usb/lp0}).</p>
 */
public class PrinterService {

    /**
     * Prints a single PDF document on the given printer device.
     *
     * @param document PDF data to print
     * @param device   printer address ({@code tcp://host:9100} or device path)
     * @param dims     label dimensions from the preset
     * @throws PrinterServiceException if the PDF is invalid or printing fails
     */
    public void print(@NotNull PdfDocument document,
                      @NotNull String device,
                      @NotNull Dimensions dims) throws PrinterServiceException {
        validateDocument(document);
        byte[] tspl = convertPdf(document, dims);
        sendToPrinter(device, tspl);
    }

    /**
     * Prints a batch of PDF documents on the given printer device.
     *
     * <p>All TSPL jobs are concatenated into a single transmission so
     * the printer receives them as one continuous stream.</p>
     *
     * @param documents PDF documents to print
     * @param device    printer address ({@code tcp://host:9100} or device path)
     * @param dims      label dimensions from the preset
     * @throws PrinterServiceException if any document is invalid or printing fails
     */
    public void printAll(@NotNull PdfDocument[] documents,
                         @NotNull String device,
                         @NotNull Dimensions dims) throws PrinterServiceException {
        if (documents == null) {
            throw new PrinterServiceException("PDF documents must not be null");
        }

        var allTspl = new java.io.ByteArrayOutputStream();
        for (PdfDocument document : documents) {
            validateDocument(document);
            byte[] tspl = convertPdf(document, dims);
            try {
                allTspl.write(tspl);
            } catch (IOException e) {
                throw new PrinterServiceException("Failed to assemble TSPL output", e);
            }
        }
        sendToPrinter(device, allTspl.toByteArray());
    }

    /**
     * Lists locally-attached raw printer devices.
     *
     * <p>Discovery is best-effort. The returned array is never null
     * but may be empty if no devices are found. Users can type custom
     * addresses (like {@code tcp://192.168.1.100:9100}) directly.</p>
     *
     * @return discovered device addresses
     */
    public static String[] listDevices() {
        return PrinterDevice.listDevices();
    }

    // ── internal ────────────────────────────────────────────────

    private void validateDocument(PdfDocument document) throws PrinterServiceException {
        if (document == null) {
            throw new PrinterServiceException("PDF document must not be null");
        }
        if (document.data() == null || document.data().length == 0) {
            throw new PrinterServiceException("PDF document data must not be empty");
        }
    }

    /**
     * Converts a single PDF document to a TSPL byte stream.
     * Multi-page PDFs produce one label per page.
     */
    private byte[] convertPdf(PdfDocument document, Dimensions dims)
            throws PrinterServiceException {
        try (PDDocument pdf = Loader.loadPDF(document.data())) {
            int pageCount = pdf.getNumberOfPages();
            if (pageCount == 0) {
                throw new PrinterServiceException("PDF document has no pages");
            }

            var out = new java.io.ByteArrayOutputStream();
            for (int i = 0; i < pageCount; i++) {
                BitmapImage bitmap = PdfBitmapRenderer.renderPage(pdf, i, dims);
                byte[] tspl = TsplLabel.generate(dims, bitmap);
                out.write(tspl);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new PrinterServiceException("Failed to load PDF document", e);
        }
    }

    private void sendToPrinter(String device, byte[] data)
            throws PrinterServiceException {
        try {
            PrinterDevice.send(device, data);
        } catch (IOException e) {
            throw new PrinterServiceException("Failed to send data to printer", e);
        }
    }
}
