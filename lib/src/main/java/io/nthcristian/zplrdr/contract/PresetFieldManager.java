package io.nthcristian.zplrdr.contract;

import java.util.Map;

import io.nthcristian.zplrdr.error.FieldValidationException;

public interface PresetFieldManager {
    void validateFor(Map<String, String> fields) throws FieldValidationException;

    Object getDefaultValueFor(String field);
}