package io.nthcristian.zplrdr.document;

import jakarta.validation.constraints.NotNull;

public record PDFDocument(@NotNull byte[] data) {
}