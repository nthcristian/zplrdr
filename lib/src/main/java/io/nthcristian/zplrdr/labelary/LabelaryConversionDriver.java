package io.nthcristian.zplrdr.labelary;

import io.nthcristian.zplrdr.contract.ConversionDriver;
import io.nthcristian.zplrdr.preset.util.MappedPreset;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;
import io.nthcristian.zplrdr.document.ZPLLabel;

public class LabelaryConversionDriver implements ConversionDriver {
    private static final int BATCH_SIZE = 50;

    @Override
    public PDFDocument[] requestConversion(ZPLDocument[] zplFiles, MappedPreset mappedPreset) {
        // TODO
        return null;
    }

    private ZPLLabel[] splitIntoBatches(ZPLDocument zplFile) {
        // TODO
        return null;
    }

    private void sendBatch(ZPLLabel[] zplBatch) {
        // TODO
    }
}