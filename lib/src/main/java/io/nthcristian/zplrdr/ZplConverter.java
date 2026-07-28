package io.nthcristian.zplrdr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.validation.constraints.NotNull;
import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.contract.ConversionProvider;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.document.ZplDocument;
import io.nthcristian.zplrdr.error.ConversionProviderException;
import io.nthcristian.zplrdr.error.ZplConverterException;

public class ZplConverter {
    private static final String ZPL_START = "^XA";
    private static final String ZPL_END = "^XZ";

    private final ConversionProvider conversionProvider;

    public ZplConverter(ConversionProvider conversionProvider) {
        this.conversionProvider = conversionProvider;
    }

    public PdfDocument[] convertAll(@NotNull InputStream[] files, @NotNull Preset preset)
            throws ZplConverterException {
        if (files.length == 0) {
            return new PdfDocument[0];
        }

        ZplDocument[] zplDocuments = new ZplDocument[files.length];
        for (int i = 0; i < files.length; i++) {
            zplDocuments[i] = parseAndValidate(files[i]);
        }

        try {
            return conversionProvider.convert(zplDocuments, preset);
        } catch (ConversionProviderException e) {
            throw new ZplConverterException("Could not convert files", e);
        }
    }

    private ZplDocument parseAndValidate(InputStream file) throws ZplConverterException {
        byte[] data;
        try {
            data = file.readAllBytes();
        } catch (IOException e) {
            throw new ZplConverterException("Failed to read input stream", e);
        }

        validateFormat(data);
        return new ZplDocument(data);
    }

    private void validateFormat(byte[] data) throws ZplConverterException {
        String content = new String(data, StandardCharsets.UTF_8);
        if (!content.startsWith(ZPL_START) || !content.endsWith(ZPL_END)) {
            throw new ZplConverterException("The file is not a valid ZPL document");
        }
    }
}