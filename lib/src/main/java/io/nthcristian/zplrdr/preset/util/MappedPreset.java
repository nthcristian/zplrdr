package io.nthcristian.zplrdr.preset.util;

import java.util.Map;

public record MappedPreset(Map<String, String> map) {
    public String getFieldValue(String key) {
        return this.map.get(key);
    }

    public String[] getFields() {
        return map.keySet().toArray(new String[0]);
    }
}
