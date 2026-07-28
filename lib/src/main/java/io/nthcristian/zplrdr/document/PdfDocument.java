package io.nthcristian.zplrdr.document;

import jakarta.validation.constraints.NotNull;

public record PdfDocument(@NotNull byte[] data) {
}