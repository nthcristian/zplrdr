package io.nthcristian.zplrdr;

import java.util.Map;

import io.nthcristian.zplrdr.contract.PresetFieldManager;
import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.preset.util.PresetParser;

public class PresetManager {
    private Map<String, Preset> presets;

    private final PresetFieldManager fieldManager;
    private final PresetParser presetParser;

    public PresetManager(PresetFieldManager fieldManager, PresetParser presetParser) {
        this.fieldManager = fieldManager;
        this.presetParser = presetParser;
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