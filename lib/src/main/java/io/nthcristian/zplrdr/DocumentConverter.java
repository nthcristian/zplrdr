package io.nthcristian.zplrdr;

import java.io.File;

import io.nthcristian.zplrdr.preset.util.MappedPreset;
import io.nthcristian.zplrdr.contract.IConversionDriver;
import io.nthcristian.zplrdr.typed.PDFDocument;
import io.nthcristian.zplrdr.typed.ZPLDocument;

public class DocumentConverter {
    private final IConversionDriver conversionDriver;

    public DocumentConverter(IConversionDriver conversionDriver) {
        this.conversionDriver = conversionDriver;
    }

    public PDFDocument[] convert(File[] files, MappedPreset mappedPreset) {
        // TODO
        return null;
    }

    private ZPLDocument[] validateFiles(String[] fileData) {
        // TODO
        return null;
    }
}
