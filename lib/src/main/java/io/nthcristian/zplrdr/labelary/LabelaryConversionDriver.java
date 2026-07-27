package io.nthcristian.zplrdr.labelary;

import io.nthcristian.zplrdr.contract.IConversionDriver;
import io.nthcristian.zplrdr.preset.util.MappedPreset;
import io.nthcristian.zplrdr.typed.PDFDocument;
import io.nthcristian.zplrdr.typed.ZPLDocument;
import io.nthcristian.zplrdr.typed.ZPLLabel;

public class LabelaryConversionDriver implements IConversionDriver {
    private final Integer batchSize = 50;

    @Override
    public PDFDocument[] requestConversion(ZPLDocument[] zplFile, MappedPreset mappedPreset) {
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
