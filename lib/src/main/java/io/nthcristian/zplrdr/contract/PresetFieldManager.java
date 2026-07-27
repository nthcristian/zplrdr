package io.nthcristian.zplrdr.contract;

import java.util.Map;

public interface PresetFieldManager {
    Map<String, Boolean> getFields();

    Object getDefaultValueFor(String field);
}