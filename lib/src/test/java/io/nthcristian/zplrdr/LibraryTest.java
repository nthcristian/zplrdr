package io.nthcristian.zplrdr;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LibraryTest {
    @Test
    void documentConverterCanBeInstantiated() {
        assertNotNull(ZplConverter.class);
    }

    @Test
    void presetManagerCanBeInstantiated() {
        assertNotNull(PresetService.class);
    }
}