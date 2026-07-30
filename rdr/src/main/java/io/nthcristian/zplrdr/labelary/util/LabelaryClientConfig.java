package io.nthcristian.zplrdr.labelary.util;

import jakarta.validation.constraints.NotBlank;

// API key can be null
public record LabelaryClientConfig(@NotBlank String baseUrl, String apiKey) {

}
