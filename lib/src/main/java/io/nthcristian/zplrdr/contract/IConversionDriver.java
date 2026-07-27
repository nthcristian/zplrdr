package io.nthcristian.zplrdr.contract;

import io.nthcristian.zplrdr.preset.util.MappedPreset;
import io.nthcristian.zplrdr.typed.PDFDocument;
import io.nthcristian.zplrdr.typed.ZPLDocument;

public interface IConversionDriver {
    public PDFDocument[] requestConversion(ZPLDocument[] zplFiles, MappedPreset mappedPreset);
}
