package io.nthcristian.zplrdr.labelary.util;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// API key can be null
public record LabelaryClientConfig(@NotBlank String baseUrl, @NotNull String apiKey) {

}
