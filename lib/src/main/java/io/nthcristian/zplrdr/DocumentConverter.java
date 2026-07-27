package io.nthcristian.zplrdr;

import java.io.File;

import io.nthcristian.zplrdr.preset.util.MappedPreset;
import io.nthcristian.zplrdr.contract.ConversionDriver;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;

public class DocumentConverter {
    private final ConversionDriver conversionDriver;

    public DocumentConverter(ConversionDriver conversionDriver) {
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