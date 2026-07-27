package io.nthcristian.zplrdr.labelary.document;

import jakarta.validation.constraints.NotNull;

public record ZPLLabel(@NotNull byte[] data) {
}