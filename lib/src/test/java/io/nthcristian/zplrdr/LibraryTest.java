package io.nthcristian.zplrdr;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LibraryTest {
    @Test
    void documentConverterCanBeInstantiated() {
        assertNotNull(DocumentConverter.class);
    }

    @Test
    void presetManagerCanBeInstantiated() {
        assertNotNull(PresetManager.class);
    }
}