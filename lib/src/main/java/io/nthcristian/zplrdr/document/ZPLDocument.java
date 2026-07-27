package io.nthcristian.zplrdr.document;

import jakarta.validation.constraints.NotNull;

public record ZPLDocument(@NotNull byte[] data) {
}