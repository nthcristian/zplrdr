package io.nthcristian.zplrdr.builder;

import java.nio.file.Path;

import io.nthcristian.zplrdr.PresetService;
import io.nthcristian.zplrdr.labelary.LabelaryPresetSchema;
import io.nthcristian.zplrdr.preset.PresetRepository;

public class PresetServiceBuilder {
    public static PresetService build() {
        var schema = new LabelaryPresetSchema();
        var repository = new PresetRepository(
                Path.of(System.getProperty("user.home"), ".local/share/zplrdr").toString());
        return new PresetService(schema, repository);
    }
}
