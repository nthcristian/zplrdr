package io.nthcristian.zplrdr.contract;

import java.util.Map;

public interface PresetFieldManager {
    void validateFor(Map<String, String> fields);

    Object getDefaultValueFor(String field);
}