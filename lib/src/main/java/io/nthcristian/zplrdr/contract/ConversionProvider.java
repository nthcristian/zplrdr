package io.nthcristian.zplrdr.contract;

import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.document.ZplDocument;
import io.nthcristian.zplrdr.error.ConversionProviderException;

public interface ConversionProvider {
    PdfDocument[] convert(ZplDocument[] zplFiles, Preset preset) throws ConversionProviderException;
}