package io.nthcristian.zplrdr.preset.util;

import java.util.Map;

public class MappedPreset {
    private final Map<String, String> map;

    public MappedPreset(Map<String, String> fields) {
        this.map = fields;
    }

    public String getFieldValue(String key) {
        return this.map.get(key);
    }

    public String[] getFields() {
        return (String[]) map.keySet().toArray();
    }
}
