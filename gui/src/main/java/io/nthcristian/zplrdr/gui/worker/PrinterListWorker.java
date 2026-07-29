package io.nthcristian.zplrdr.gui.worker;

import java.util.function.BiConsumer;

import javax.swing.SwingWorker;

import io.nthcristian.zplrdr.gui.service.ServiceProvider;

/**
 * Retrieves the list of available system printers on a background thread.
 *
 * <p>Delegates to {@link io.nthcristian.prt.PrinterService#listPrinters()}
 * which calls the Java Print Service lookup API. The returned
 * {@code String[]} is delivered on the EDT via the callback provided
 * at construction.</p>
 */
public class PrinterListWorker extends SwingWorker<String[], Void> {

    private final BiConsumer<String[], Throwable> callback;

    /**
     * Creates a printer list worker.
     *
     * @param callback called on the EDT when the lookup completes or fails;
     *                 receives the printer name array on success or an
     *                 exception on error
     */
    public PrinterListWorker(BiConsumer<String[], Throwable> callback) {
        this.callback = callback;
    }

    /**
     * Called on a background thread. Queries the Java Print Service for
     * available printer names.
     *
     * @return an array of printer names (empty array if none found)
     * @throws Exception if the lookup fails
     */
    @Override
    protected String[] doInBackground() throws Exception {
        return ServiceProvider.printerService().listPrinters();
    }

    /**
     * Called on the EDT after {@code doInBackground} completes.
     * Delivers results or errors to the callback.
     */
    @Override
    protected void done() {
        try {
            String[] result = get();
            if (callback != null) {
                callback.accept(result, null);
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.accept(null, e.getCause() != null ? e.getCause() : e);
            }
        }
    }
}
