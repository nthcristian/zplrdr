package io.nthcristian.zplrdr.gui.worker;

import java.util.function.BiConsumer;

import javax.swing.SwingWorker;

import io.nthcristian.prt.Dimensions;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.gui.service.ServiceProvider;

/**
 * Prints one or more PDF documents on a background thread.
 *
 * <p>Converts each PDF page to a TSPL bitmap and sends raw commands
 * to the printer device. The result (or error) is delivered on the EDT
 * via the callback provided at construction.</p>
 */
public class PrintWorker extends SwingWorker<Void, Void> {

    private final PdfDocument[] documents;
    private final String device;
    private final Dimensions dims;
    private final BiConsumer<Void, Throwable> callback;

    /**
     * Creates a print worker.
     *
     * @param documents PDFs to print
     * @param device    printer device address (tcp://host:9100 or device path)
     * @param dims      label dimensions from the preset
     * @param callback  called on the EDT when printing completes or fails
     */
    public PrintWorker(PdfDocument[] documents, String device, Dimensions dims,
                       BiConsumer<Void, Throwable> callback) {
        this.documents = documents;
        this.device = device;
        this.dims = dims;
        this.callback = callback;
    }

    /**
     * Called on a background thread. Submits TSPL commands to the
     * printer via {@code PrinterService}.
     *
     * @return null
     * @throws Exception if printing fails
     */
    @Override
    protected Void doInBackground() throws Exception {
        ServiceProvider.printerService().printAll(documents, device, dims);
        return null;
    }

    /**
     * Called on the EDT after {@code doInBackground} completes.
     * Delivers results or errors to the callback.
     */
    @Override
    protected void done() {
        try {
            get();
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
