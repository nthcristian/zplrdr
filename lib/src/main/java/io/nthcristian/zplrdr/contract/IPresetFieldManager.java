package io.nthcristian.zplrdr.contract;

import java.util.Map;

public interface IPresetFieldManager {
    public Map<String, Boolean> getFields();

    public Object getDefaultValueFor(String field);
}
