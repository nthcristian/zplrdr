package io.nthcristian.zplrdr.preset.util;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

public record Preset(
        @NotBlank String name,
        @NotNull Map<String, String> fields) {

    public Preset {
        fields = Map.copyOf(fields);
    }

    public String getFieldValue(@NotNull String field) {
        return fields().get(field);
    }

    public Preset withFieldValue(@NotBlank String field, @NotNull String value) {
        Map<String, String> modified = new HashMap<>(fields());
        modified.put(field, value);
        return new Preset(name(), modified);
    }
}