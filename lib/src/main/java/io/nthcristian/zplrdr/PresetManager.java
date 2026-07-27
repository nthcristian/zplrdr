package io.nthcristian.zplrdr;

import java.util.HashMap;
import java.util.Map;

import io.nthcristian.zplrdr.contract.PresetFieldManager;
import io.nthcristian.zplrdr.error.FieldValidationException;
import io.nthcristian.zplrdr.error.PresetFileStoreException;
import io.nthcristian.zplrdr.error.PresetManagerException;
import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.preset.util.PresetFileStore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PresetManager {
    private final Map<String, Preset> presets;

    private final PresetFieldManager fieldManager;
    private final PresetFileStore presetFileStore;

    public PresetManager(
            @NotNull PresetFieldManager fieldManager,
            @NotNull PresetFileStore presetFileStore) {
        this.fieldManager = fieldManager;
        this.presetFileStore = presetFileStore;
        this.presets = new HashMap<>();

        loadPresets();
    }

    public Preset getPreset(@NotBlank String name) {
        return presets.get(name);
    }

    public Preset createPreset(@NotBlank String name) throws PresetManagerException {
        if (presets.containsKey(name)) {
            throw new PresetManagerException("Preset with name '%s' already exists".formatted(name));
        }

        Map<String, String> fields = new HashMap<>();
        for (String fieldName : fieldManager.getFieldNames()) {
            Object defaultValue = fieldManager.getDefaultValueFor(fieldName);
            fields.put(fieldName, defaultValue != null ? defaultValue.toString() : "");
        }

        Preset preset = new Preset(name, fields);

        try {
            fieldManager.validateFor(preset.fields());
        } catch (FieldValidationException e) {
            throw new PresetManagerException("Created preset with invalid defaults", e);
        }

        return preset;
    }

    public Preset persistPreset(@NotNull Preset preset) throws PresetManagerException {
        try {
            fieldManager.validateFor(preset.fields());
        } catch (FieldValidationException e) {
            throw new PresetManagerException("Could not update/create preset", e);
        }

        presets.put(preset.name(), preset);
        try {
            presetFileStore.save(preset.name(), preset.fields());
        } catch (PresetFileStoreException e) {
            throw new PresetManagerException("Failed to persist preset", e);
        }

        return preset;
    }

    public void deletePreset(@NotBlank String name) throws PresetManagerException {
        if (!presets.containsKey(name)) {
            throw new PresetManagerException("No preset found with name '%s'".formatted(name));
        }

        presets.remove(name);
        try {
            presetFileStore.delete(name);
        } catch (PresetFileStoreException e) {
            throw new PresetManagerException("Failed to delete preset", e);
        }
    }

    private void loadPresets() {
        Map<String, Map<String, String>> loaded;
        try {
            loaded = presetFileStore.loadAll();
        } catch (PresetFileStoreException e) {
            return; // directory may not exist yet, start with empty presets
        }
        for (Map.Entry<String, Map<String, String>> entry : loaded.entrySet()) {
            Preset preset = new Preset(entry.getKey(), entry.getValue());
            try {
                fieldManager.validateFor(preset.fields());
            } catch (FieldValidationException e) {
                continue;
            }
            presets.put(preset.name(), preset);
        }
    }
}