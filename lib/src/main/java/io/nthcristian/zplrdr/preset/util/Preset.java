package io.nthcristian.zplrdr.preset.util;

import java.util.Map;

public record Preset(String name, Map<String, String> fields) {

    public Preset {
        fields = Map.copyOf(fields);
    }

    public String getFieldValue(String field) {
        // TODO
        return null;
    }

    public Preset setFieldValue(String field, String value) {
        // TODO
        return null;
    }
}