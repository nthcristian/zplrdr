package io.nthcristian.zplrdr.contract;

import java.util.Map;
import java.util.Set;

import io.nthcristian.zplrdr.error.PresetSchemaException;

public interface PresetSchema {
    void validate(Map<String, String> fields) throws PresetSchemaException;

    Object getDefaultValue(String field);

    Set<String> getFieldNames();
}
