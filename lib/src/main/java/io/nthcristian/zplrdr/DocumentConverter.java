package io.nthcristian.zplrdr;

import java.io.InputStream;

import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.contract.ConversionDriver;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;

public class DocumentConverter {
    private final ConversionDriver conversionDriver;

    public DocumentConverter(ConversionDriver conversionDriver) {
        this.conversionDriver = conversionDriver;
    }

    public PDFDocument[] convert(InputStream[] files, Preset preset) {
        // TODO
        return null;
    }

    private ZPLDocument[] validateFiles(String[] fileData) {
        // TODO
        return null;
    }
}