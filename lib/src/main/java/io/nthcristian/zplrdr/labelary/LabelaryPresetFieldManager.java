package io.nthcristian.zplrdr.labelary;

import java.util.Map;

import io.nthcristian.zplrdr.contract.PresetFieldManager;

public class LabelaryPresetFieldManager implements PresetFieldManager {
    private final Map<String, FieldDefinition> fields = Map.of();

    private record FieldDefinition(Object defaultValue, Boolean optional) {
    }

    @Override
    public Object getDefaultValueFor(String field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validateFor(Map<String, String> fields) {
        // TODO Auto-generated method stub

    }
}