package io.nthcristian.zplrdr.preset;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@DisplayName("Preset")
class PresetTest {

    private final Map<String, String> sampleFields = Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9");

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should store name and fields")
        void shouldStoreNameAndFields() {
            Preset preset = new Preset("test-preset", sampleFields);

            assertEquals("test-preset", preset.name());
            assertEquals(sampleFields, preset.fields());
        }

        @Test
        @DisplayName("should create a defensive copy of fields")
        void shouldDefensiveCopyFields() {
            Map<String, String> mutableFields = new HashMap<>(sampleFields);
            Preset preset = new Preset("test-preset", mutableFields);

            assertNotSame(mutableFields, preset.fields());

            mutableFields.put("dpmm", "12dpmm");
            assertEquals("8dpmm", preset.fields().get("dpmm"),
                    "modifying the original map should not affect the Preset");
        }

        @Test
        @DisplayName("should return read-only fields map")
        void shouldReturnReadOnlyFieldsMap() {
            Preset preset = new Preset("test-preset", sampleFields);

            assertThrows(UnsupportedOperationException.class,
                    () -> preset.fields().put("dpmm", "12dpmm"));
        }

        @Test
        @DisplayName("should accept empty fields map")
        void shouldAcceptEmptyFieldsMap() {
            Preset preset = new Preset("empty-preset", Map.of());

            assertEquals(0, preset.fields().size());
        }
    }

    @Nested
    @DisplayName("getProperty")
    class GetProperty {

        @Test
        @DisplayName("should return value for existing field")
        void shouldReturnValueForExistingField() {
            Preset preset = new Preset("test-preset", sampleFields);

            assertEquals("8dpmm", preset.getProperty("dpmm"));
            assertEquals("5.9", preset.getProperty("width"));
            assertEquals("3.9", preset.getProperty("height"));
        }

        @Test
        @DisplayName("should return null for missing field")
        void shouldReturnNullForMissingField() {
            Preset preset = new Preset("test-preset", sampleFields);

            assertNull(preset.getProperty("nonexistent"));
        }
    }

    @Nested
    @DisplayName("withProperty")
    class WithProperty {

        @Test
        @DisplayName("should return new Preset with modified field")
        void shouldReturnNewPresetWithModifiedField() {
            Preset original = new Preset("test-preset", sampleFields);
            Preset modified = original.withProperty("dpmm", "12dpmm");

            // original unchanged
            assertEquals("8dpmm", original.getProperty("dpmm"));
            // new value on modified
            assertEquals("12dpmm", modified.getProperty("dpmm"));
        }

        @Test
        @DisplayName("should preserve name on modified preset")
        void shouldPreserveNameOnModifiedPreset() {
            Preset original = new Preset("test-preset", sampleFields);
            Preset modified = original.withProperty("width", "8.0");

            assertEquals("test-preset", modified.name());
        }

        @Test
        @DisplayName("should preserve other fields on modified preset")
        void shouldPreserveOtherFields() {
            Preset original = new Preset("test-preset", sampleFields);
            Preset modified = original.withProperty("dpmm", "12dpmm");

            assertEquals("5.9", modified.getProperty("width"));
            assertEquals("3.9", modified.getProperty("height"));
        }

        @Test
        @DisplayName("should add new field when calling withProperty on missing field")
        void shouldAddNewField() {
            Preset original = new Preset("test-preset", sampleFields);
            Preset modified = original.withProperty("newField", "newValue");

            assertEquals("newValue", modified.getProperty("newField"));
            assertNull(original.getProperty("newField"));
        }

        @Test
        @DisplayName("should be immutable — returned preset should also be immutable")
        void shouldReturnImmutablePreset() {
            Preset original = new Preset("test-preset", sampleFields);
            Preset modified = original.withProperty("dpmm", "12dpmm");

            assertThrows(UnsupportedOperationException.class,
                    () -> modified.fields().put("width", "10.0"));
        }
    }
}