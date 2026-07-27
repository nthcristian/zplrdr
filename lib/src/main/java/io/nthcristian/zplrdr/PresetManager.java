package io.nthcristian.zplrdr;

import java.util.Map;

import io.nthcristian.zplrdr.contract.PresetFieldManager;
import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.preset.util.PresetFileStore;

public class PresetManager {
    private Map<String, Preset> presets;

    private final PresetFieldManager fieldManager;
    private final PresetFileStore presetFileStore;

    public PresetManager(PresetFieldManager fieldManager, PresetFileStore presetFileStore) {
        this.fieldManager = fieldManager;
        this.presetFileStore = presetFileStore;
    }

    public Preset getPreset(String name) {
        // TODO
        return null;
    }

    // Create or update
    public Preset setPreset(Preset preset) {
        // TODO
        return null;
    }

    public void deletePreset(String name) {
        // TODO
    }

    private void loadPresets() {
        // TODO
    }
}