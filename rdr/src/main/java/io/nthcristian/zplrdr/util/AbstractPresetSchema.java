package io.nthcristian.zplrdr.util;

import java.util.Map;
import java.util.Set;

import io.nthcristian.zplrdr.contract.PresetSchema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public abstract class AbstractPresetSchema implements PresetSchema {
    private final Map<String, FieldDefinition> requiredFields;

    protected final record FieldDefinition(@NotNull Object defaultValue) {
        public static FieldDefinition of(@NotNull Object defaultValue) {
            return new FieldDefinition(defaultValue);
        }
    }

    protected AbstractPresetSchema(@NotNull Map<String, FieldDefinition> requiredFields) {
        this.requiredFields = requiredFields;
    }

    @Override
    public Object getDefaultValue(@NotBlank String field) {
        var definition = requiredFields.get(field);
        if (definition == null) {
            return null;
        }

        var defaultValue = definition.defaultValue;

        return defaultValue;
    }

    @Override
    public Set<String> getFieldNames() {
        return Set.copyOf(requiredFields.keySet());
    }

    protected Map<String, FieldDefinition> getRequiredFieldDefinitions() {
        return Map.copyOf(requiredFields);
    }
}
