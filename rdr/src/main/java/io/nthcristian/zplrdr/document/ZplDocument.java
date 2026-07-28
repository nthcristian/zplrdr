package io.nthcristian.zplrdr.document;

import jakarta.validation.constraints.NotNull;

public record ZplDocument(@NotNull byte[] data) {
}