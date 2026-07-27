package io.nthcristian.zplrdr.labelary;

import java.util.Map;

import io.nthcristian.zplrdr.contract.IPresetFieldManager;

public class LabelaryPresetFieldManager implements IPresetFieldManager {
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
