package io.nthcristian.zplrdr.labelary.document;

import jakarta.validation.constraints.NotNull;

public record ZplLabel(@NotNull byte[] data) {
}