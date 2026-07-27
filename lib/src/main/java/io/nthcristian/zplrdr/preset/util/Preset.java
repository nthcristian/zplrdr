package io.nthcristian.zplrdr.preset.util;

import java.util.Map;

public record Preset(String name, Map<String, Boolean> fields, PresetParser presetParser) {
    public String getFieldValue(String field) {
        // TODO
        return null;
    }

    public void setFieldValue(String field, String value) {
        // TODO
    }

    public MappedPreset getMapping() {
        // TODO
        return null;
    }
}
