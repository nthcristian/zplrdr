package io.nthcristian.zplrdr.builder;

import io.nthcristian.zplrdr.ZplConverter;
import io.nthcristian.zplrdr.labelary.LabelaryConversionProvider;

public class ZplConverterBuilder {
    public static ZplConverter build() {
        var conversionProvider = new LabelaryConversionProvider();
        return new ZplConverter(conversionProvider);
    }
}
