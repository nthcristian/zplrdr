package io.nthcristian.zplrdr.preset.util;

public class Preset {
    private final String name;
    private final PresetParser presetParser;

    public Preset(String name, PresetParser presetParser) {
        this.name = name;
        this.presetParser = presetParser;
    }

    public String getFieldValue(String field) {
        // TODO
        return null;
    }

    public void setFieldValue(String field, String value) {
        // TODO
    }

    public MappedPreset toMappedPreset() {
        // TODO
        return null;
    }
}