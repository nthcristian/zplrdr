package io.nthcristian.zplrdr.contract;

import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.document.ZplDocument;
import io.nthcristian.zplrdr.error.ConversionProviderException;
import io.nthcristian.zplrdr.preset.Preset;

public interface ConversionProvider {
    PdfDocument[] convert(ZplDocument[] zplFiles, Preset preset) throws ConversionProviderException;
}