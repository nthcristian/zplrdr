package io.nthcristian.zplrdr.gui.worker;

import java.util.function.BiConsumer;

import javax.swing.SwingWorker;

import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.gui.service.ServiceProvider;

/**
 * Prints one or more PDF documents on a background thread.
 *
 * <p>Delegates to {@link io.nthcristian.prt.PrinterService#printAll(PdfDocument[])}
 * or {@link io.nthcristian.prt.PrinterService#printAll(PdfDocument[], String)}
 * depending on whether a printer name was specified. The result (or error)
 * is delivered on the EDT via the callback provided at construction.</p>
 */
public class PrintWorker extends SwingWorker<Void, Void> {

    private final PdfDocument[] documents;
    private final String printerName;
    private final BiConsumer<Void, Throwable> callback;

    /**
     * Creates a print worker.
     *
     * @param documents   PDFs to print
     * @param printerName target printer, or {@code null} for system default
     * @param callback    called on the EDT when printing completes or fails;
     *                    receives null on success or an exception on error
     */
    public PrintWorker(PdfDocument[] documents, String printerName,
                       BiConsumer<Void, Throwable> callback) {
        this.documents = documents;
        this.printerName = printerName;
        this.callback = callback;
    }

    /**
     * Called on a background thread. Submits the print job via
     * {@code PrinterService}.
     *
     * @return null
     * @throws Exception if printing fails
     */
    @Override
    protected Void doInBackground() throws Exception {
        var printerService = ServiceProvider.printerService();
        if (printerName == null || printerName.isBlank()) {
            printerService.printAll(documents);
        } else {
            printerService.printAll(documents, printerName);
        }
        return null;
    }

    /**
     * Called on the EDT after {@code doInBackground} completes.
     * Delivers results or errors to the callback.
     */
    @Override
    protected void done() {
        try {
            get(); // check for exception
            if (callback != null) {
                callback.accept(null, null);
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.accept(null, e.getCause() != null ? e.getCause() : e);
            }
        }
    }
}
