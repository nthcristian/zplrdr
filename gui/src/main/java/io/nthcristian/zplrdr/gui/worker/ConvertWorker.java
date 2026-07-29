package io.nthcristian.zplrdr.gui.worker;

import java.io.InputStream;
import java.util.function.BiConsumer;

import javax.swing.SwingWorker;

import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.gui.service.ServiceProvider;
import io.nthcristian.zplrdr.preset.Preset;

/**
 * Converts ZPL label files to PDF documents on a background thread.
 *
 * <p>Delegates to {@link io.nthcristian.zplrdr.ZplConverter#convertAll}
 * and delivers the resulting {@code PdfDocument[]} on the EDT via
 * the callback provided at construction. Progress can be monitored via
 * the {@code SwingWorker} property change mechanism.</p>
 *
 * <p>Streams passed to the constructor are closed by the conversion
 * call if it completes successfully; otherwise the caller should
 * close them in the callback on error.</p>
 */
public class ConvertWorker extends SwingWorker<PdfDocument[], Void> {

    private final InputStream[] files;
    private final Preset preset;
    private final BiConsumer<PdfDocument[], Throwable> callback;

    /**
     * Creates a conversion worker for the given ZPL files and preset.
     *
     * @param files    ZPL file input streams (ownership transfers to this worker)
     * @param preset   preset with formatting parameters (dpmm, width, height)
     * @param callback called on the EDT when conversion completes or fails;
     *                 receives the result array on success or an exception on error
     */
    public ConvertWorker(InputStream[] files, Preset preset,
                         BiConsumer<PdfDocument[], Throwable> callback) {
        this.files = files;
        this.preset = preset;
        this.callback = callback;
    }

    /**
     * Called on a background thread. Invokes the Labelary API via
     * {@code ZplConverter.convertAll}.
     *
     * @return the converted PDF documents
     * @throws Exception if conversion fails
     */
    @Override
    protected PdfDocument[] doInBackground() throws Exception {
        return ServiceProvider.zplConverter().convertAll(files, preset);
    }

    /**
     * Called on the EDT after {@code doInBackground} completes.
     * Delivers results or errors to the callback.
     */
    @Override
    protected void done() {
        try {
            PdfDocument[] result = get();
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
