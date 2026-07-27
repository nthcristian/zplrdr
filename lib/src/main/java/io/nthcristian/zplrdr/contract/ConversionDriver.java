package io.nthcristian.zplrdr.contract;

import io.nthcristian.zplrdr.preset.util.MappedPreset;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;

public interface ConversionDriver {
    PDFDocument[] requestConversion(ZPLDocument[] zplFiles, MappedPreset mappedPreset);
}