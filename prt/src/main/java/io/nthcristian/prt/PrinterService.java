package io.nthcristian.prt;

import java.io.IOException;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintRequestAttributeSet;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import jakarta.validation.constraints.NotNull;
import io.nthcristian.prt.error.PrinterServiceException;
import io.nthcristian.zplrdr.document.PdfDocument;

public class PrinterService {

    static {
        // Allow print APIs without a graphical display (servers/containers).
        System.setProperty("java.awt.headless", "true");
    }

    public void print(@NotNull PdfDocument document) throws PrinterServiceException {
        validateDocument(document);
        printValidated(document, resolveDefaultPrintService());
    }

    public void print(@NotNull PdfDocument document, @NotNull String printerName)
            throws PrinterServiceException {
        validateDocument(document);
        printValidated(document, resolvePrintService(printerName));
    }

    public void printAll(@NotNull PdfDocument[] documents) throws PrinterServiceException {
        if (documents == null) {
            throw new PrinterServiceException("PDF documents must not be null");
        }
        for (PdfDocument document : documents) {
            validateDocument(document);
        }
        PrintService printService = resolveDefaultPrintService();
        for (PdfDocument document : documents) {
            printValidated(document, printService);
        }
    }

    public void printAll(@NotNull PdfDocument[] documents, @NotNull String printerName)
            throws PrinterServiceException {
        if (documents == null) {
            throw new PrinterServiceException("PDF documents must not be null");
        }
        for (PdfDocument document : documents) {
            validateDocument(document);
        }
        PrintService printService = resolvePrintService(printerName);
        for (PdfDocument document : documents) {
            printValidated(document, printService);
        }
    }

    public String[] listPrinters() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        String[] names = new String[services.length];
        for (int i = 0; i < services.length; i++) {
            names[i] = services[i].getName();
        }
        return names;
    }

    private void validateDocument(PdfDocument document) throws PrinterServiceException {
        if (document == null) {
            throw new PrinterServiceException("PDF document must not be null");
        }
        if (document.data() == null || document.data().length == 0) {
            throw new PrinterServiceException("PDF document data must not be empty");
        }
    }

    private void printValidated(PdfDocument document, PrintService printService)
            throws PrinterServiceException {
        try (PDDocument pdDocument = Loader.loadPDF(document.data())) {
            if (pdDocument.getNumberOfPages() == 0) {
                throw new PrinterServiceException("PDF document has no pages");
            }

            PrintRequestAttributeSet attrs = LabelPrintLayout.createAttributes(pdDocument);
            DocPrintJob job = printService.createPrintJob();
            job.print(
                    new SimpleDoc(
                            LabelPrintLayout.createPageable(pdDocument),
                            DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                            null),
                    attrs);
        } catch (IOException e) {
            throw new PrinterServiceException("Failed to load PDF document", e);
        } catch (PrintException e) {
            throw new PrinterServiceException("Failed to print PDF document", e);
        }
    }

    private PrintService resolveDefaultPrintService() throws PrinterServiceException {
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        if (printService == null) {
            throw new PrinterServiceException("No default print service is available");
        }
        return printService;
    }

    private PrintService resolvePrintService(String printerName) throws PrinterServiceException {
        if (printerName == null || printerName.isBlank()) {
            throw new PrinterServiceException("Printer name must not be blank");
        }

        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }

        throw new PrinterServiceException("Printer not found: " + printerName);
    }

}
