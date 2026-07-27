package io.nthcristian.zplrdr;

import java.util.Map;

import io.nthcristian.zplrdr.contract.IPresetFieldManager;
import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.preset.util.PresetParser;

public class PresetManager {
    private Map<String, Preset> presets;

    private final IPresetFieldManager fieldManager;
    private final PresetParser presetParser;

    public PresetManager(IPresetFieldManager fieldManager, PresetParser presetParser) {
        this.fieldManager = fieldManager;
        this.presetParser = presetParser;
    }

    public Preset getPreset(String name) {
        // TODO
        return null;
    }

    public Preset createPreset(String name) {
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
