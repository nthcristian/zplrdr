package io.nthcristian.zplrdr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.validation.constraints.NotNull;
import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.contract.ConversionDriver;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;
import io.nthcristian.zplrdr.error.ConversionException;
import io.nthcristian.zplrdr.error.DocumentConverterException;

public class DocumentConverter {
    private static final String ZPL_START = "^XA";
    private static final String ZPL_END = "^XZ";

    private final ConversionDriver conversionDriver;

    public DocumentConverter(ConversionDriver conversionDriver) {
        this.conversionDriver = conversionDriver;
    }

    public PDFDocument[] convert(@NotNull InputStream[] files, @NotNull Preset preset)
            throws DocumentConverterException {
        if (files.length == 0) {
            return new PDFDocument[0];
        }

        ZPLDocument[] zplDocuments = new ZPLDocument[files.length];
        for (int i = 0; i < files.length; i++) {
            zplDocuments[i] = parseAndValidate(files[i]);
        }

        try {
            return conversionDriver.requestConversion(zplDocuments, preset);
        } catch (ConversionException e) {
            throw new DocumentConverterException("Could not convert files", e);
        }
    }

    private ZPLDocument parseAndValidate(InputStream file) throws DocumentConverterException {
        byte[] data;
        try {
            data = file.readAllBytes();
        } catch (IOException e) {
            throw new DocumentConverterException("Failed to read input stream", e);
        }

        validateZplContent(data);
        return new ZPLDocument(data);
    }

    private void validateZplContent(byte[] data) throws DocumentConverterException {
        String content = new String(data, StandardCharsets.UTF_8);
        if (!content.startsWith(ZPL_START) || !content.endsWith(ZPL_END)) {
            throw new DocumentConverterException("The file is not a valid ZPL document");
        }
    }
}