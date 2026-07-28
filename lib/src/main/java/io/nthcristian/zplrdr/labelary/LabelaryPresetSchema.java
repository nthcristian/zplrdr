package io.nthcristian.zplrdr.labelary;

import java.util.Map;
import java.util.regex.Pattern;

import io.nthcristian.zplrdr.error.PresetSchemaException;
import io.nthcristian.zplrdr.util.AbstractPresetSchema;

public class LabelaryPresetSchema extends AbstractPresetSchema {

    public LabelaryPresetSchema() {
        Map<String, FieldDefinition> requiredFields = Map.ofEntries(
                Map.entry("dpmm", FieldDefinition.of("8dpmm")),
                Map.entry("width", FieldDefinition.of(5.9)),
                Map.entry("height", FieldDefinition.of(3.9)));

        super(requiredFields);
    }

    private static final Pattern DPMM_PATTERN = Pattern.compile("^\\d+dpmm$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    @Override
    public void validate(Map<String, String> fields) throws PresetSchemaException {
        for (var entry : getRequiredFieldDefinitions().entrySet()) {
            String fieldName = entry.getKey();
            String value = fields.get(fieldName);

            if (value == null) {
                throw new PresetSchemaException(
                        "Required field '%s' is missing".formatted(fieldName));
            }

            if (value.isBlank()) {
                throw new PresetSchemaException(
                        "Required field '%s' is blank".formatted(fieldName));
            }

            switch (fieldName) {
                case "dpmm" -> {
                    if (!DPMM_PATTERN.matcher(value).matches()) {
                        throw new PresetSchemaException(
                                "Field '%s' must match pattern '<number>dpmm' (e.g., '8dpmm'), got: '%s'"
                                        .formatted(fieldName, value));
                    }
                }
                case "width", "height" -> {
                    if (!NUMERIC_PATTERN.matcher(value).matches()) {
                        throw new PresetSchemaException(
                                "Field '%s' must be a numeric value, got: '%s'"
                                        .formatted(fieldName, value));
                    }
                }
            }
        }
    }
}