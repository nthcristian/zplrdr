package io.nthcristian.zplrdr.preset.util;

import java.util.Map;

public class PresetParser {
    private final String presetFolderPath;

    public PresetParser(String presetFolderPath) {
        this.presetFolderPath = presetFolderPath;
    }

    // TODO create smart caching for preset fields and values

    public String getFieldValue(String presetName, String field) {
        // TODO
        return null;
    }

    // Should handle file creation
    public void setFieldValue(String presetName, String field, String value) {
        // TODO
    }

    public String[] getFields(String presetName) {
        // TODO
        return null;
    }

    public Map<String, String> getMapped() {
        // TOOD
        return null;
    }
}
