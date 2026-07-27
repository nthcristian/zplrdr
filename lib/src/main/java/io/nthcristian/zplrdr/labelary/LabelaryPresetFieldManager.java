package io.nthcristian.zplrdr.labelary;

import java.util.Map;

import io.nthcristian.zplrdr.contract.PresetFieldManager;

public class LabelaryPresetFieldManager implements PresetFieldManager {
    private final Map<String, FieldDefinition> fields = Map.of();

    private record FieldDefinition(Object defaultValue, Boolean optional) {
    }

    @Override
    public Map<String, Boolean> getFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getDefaultValueFor(String field) {
        // TODO Auto-generated method stub
        return null;
    }
}