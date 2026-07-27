package io.nthcristian.zplrdr.contract;

import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;
import io.nthcristian.zplrdr.error.ConversionException;

public interface ConversionDriver {
    PDFDocument[] requestConversion(ZPLDocument[] zplFiles, Preset preset) throws ConversionException;
}