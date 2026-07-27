package io.nthcristian.zplrdr.preset.util;

import java.util.Map;

public class PresetFileStore {
    private final String storeFolderPath;

    public PresetFileStore(String storeFolderPath) {
        this.storeFolderPath = storeFolderPath;
    }

    // TODO create smart caching for preset fields and values

    public Map<String, String> load(String presetName) {
        // TODO
        return null;
    }

    public void save(Map<String, String> fields) {
        // TODO
    }
}
