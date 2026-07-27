package io.nthcristian.zplrdr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.contract.ConversionDriver;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;
import io.nthcristian.zplrdr.error.FileValidationException;

public class DocumentConverter {
    private static final String ZPL_START = "^XA";
    private static final String ZPL_END = "^XZ";

    private final ConversionDriver conversionDriver;

    public DocumentConverter(ConversionDriver conversionDriver) {
        this.conversionDriver = Objects.requireNonNull(conversionDriver, "conversionDriver must not be null");
    }

    public PDFDocument[] convert(InputStream[] files, Preset preset) throws FileValidationException {
        Objects.requireNonNull(files, "files must not be null");
        Objects.requireNonNull(preset, "preset must not be null");
        if (files.length == 0) {
            return new PDFDocument[0];
        }

        ZPLDocument[] zplDocuments = new ZPLDocument[files.length];
        for (int i = 0; i < files.length; i++) {
            zplDocuments[i] = parseAndValidate(files[i]);
        }

        return conversionDriver.requestConversion(zplDocuments, preset);
    }

    private ZPLDocument parseAndValidate(InputStream file) throws FileValidationException {
        byte[] data;
        try {
            data = file.readAllBytes();
        } catch (IOException e) {
            throw new FileValidationException("Failed to read input stream", e);
        }

        validateZplContent(data);
        return new ZPLDocument(data);
    }

    private void validateZplContent(byte[] data) throws FileValidationException {
        String content = new String(data, StandardCharsets.UTF_8);
        if (!content.startsWith(ZPL_START) || !content.endsWith(ZPL_END)) {
            throw new FileValidationException("The file is not a valid ZPL document");
        }
    }
}