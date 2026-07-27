package io.nthcristian.zplrdr.contract;

import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;

public interface ConversionDriver {
    PDFDocument[] requestConversion(ZPLDocument[] zplFiles, Preset preset);
}