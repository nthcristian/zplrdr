package io.nthcristian.zplrdr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.nthcristian.zplrdr.contract.PresetSchema;
import io.nthcristian.zplrdr.error.PresetSchemaException;
import io.nthcristian.zplrdr.error.PresetStorageException;
import io.nthcristian.zplrdr.error.PresetServiceException;
import io.nthcristian.zplrdr.preset.Preset;
import io.nthcristian.zplrdr.preset.PresetRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PresetService {
    private final Map<String, Preset> activePresets;

    private final PresetSchema schema;
    private final PresetRepository repository;

    public PresetService(
            @NotNull PresetSchema schema,
            @NotNull PresetRepository repository) {
        this.schema = schema;
        this.repository = repository;
        this.activePresets = new HashMap<>();

        initializeCache();
    }

    public Preset getPreset(@NotBlank String name) {
        return activePresets.get(name);
    }

    public List<Preset> listPresets() {
        return List.copyOf(activePresets.values());
    }

    public Preset createPreset(@NotBlank String name) throws PresetServiceException {
        if (activePresets.containsKey(name)) {
            throw new PresetServiceException("Preset with name '%s' already exists".formatted(name));
        }

        Map<String, String> fields = new HashMap<>();
        for (String fieldName : schema.getFieldNames()) {
            Object defaultValue = schema.getDefaultValue(fieldName);
            fields.put(fieldName, defaultValue != null ? defaultValue.toString() : "");
        }

        Preset preset = new Preset(name, fields);

        try {
            schema.validate(preset.fields());
        } catch (PresetSchemaException e) {
            throw new PresetServiceException("Created preset with invalid defaults", e);
        }

        return preset;
    }

    public Preset savePreset(@NotNull Preset preset) throws PresetServiceException {
        try {
            schema.validate(preset.fields());
        } catch (PresetSchemaException e) {
            throw new PresetServiceException("Could not update/create preset", e);
        }

        activePresets.put(preset.name(), preset);
        try {
            repository.save(preset.name(), preset.fields());
        } catch (PresetStorageException e) {
            throw new PresetServiceException("Failed to persist preset", e);
        }

        return preset;
    }

    public void deletePreset(@NotBlank String name) throws PresetServiceException {
        if (!activePresets.containsKey(name)) {
            throw new PresetServiceException("No preset found with name '%s'".formatted(name));
        }

        activePresets.remove(name);
        try {
            repository.deleteByName(name);
        } catch (PresetStorageException e) {
            throw new PresetServiceException("Failed to delete preset", e);
        }
    }

    private void initializeCache() {
        Map<String, Map<String, String>> loaded;
        try {
            loaded = repository.findAll();
        } catch (PresetStorageException e) {
            return; // directory may not exist yet, start with empty presets
        }
        for (Map.Entry<String, Map<String, String>> entry : loaded.entrySet()) {
            Preset preset = new Preset(entry.getKey(), entry.getValue());
            try {
                schema.validate(preset.fields());
            } catch (PresetSchemaException e) {
                continue;
            }
            activePresets.put(preset.name(), preset);
        }
    }
}