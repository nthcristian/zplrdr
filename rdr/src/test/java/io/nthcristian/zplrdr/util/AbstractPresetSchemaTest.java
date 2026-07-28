package io.nthcristian.zplrdr.util;

import static org.junit.jupiter.api.Assertions.*;

import io.nthcristian.zplrdr.contract.PresetSchema;
import io.nthcristian.zplrdr.error.PresetSchemaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

@DisplayName("AbstractPresetSchema")
class AbstractPresetSchemaTest {

    /**
     * A minimal concrete implementation for testing the abstract base.
     * This subclass exposes the protected members for testing.
     */
    static class TestSchema extends AbstractPresetSchema {
        TestSchema(Map<String, FieldDefinition> requiredFields) {
            super(requiredFields);
        }

        @Override
        public void validate(Map<String, String> fields) throws PresetSchemaException {
            // no-op pass-through
        }

        // Expose protected method for testing
        @Override
        public Map<String, FieldDefinition> getRequiredFieldDefinitions() {
            return super.getRequiredFieldDefinitions();
        }
    }

    private static AbstractPresetSchema.FieldDefinition def(Object value) {
        return AbstractPresetSchema.FieldDefinition.of(value);
    }

    @Nested
    @DisplayName("getFieldNames")
    class GetFieldNames {

        @Test
        @DisplayName("should return all registered field names")
        void shouldReturnAllRegisteredFieldNames() {
            PresetSchema schema = new TestSchema(Map.of(
                    "dpmm", def("8dpmm"),
                    "width", def(5.9),
                    "height", def(3.9)));

            Set<String> names = schema.getFieldNames();
            assertEquals(3, names.size());
            assertTrue(names.contains("dpmm"));
            assertTrue(names.contains("width"));
            assertTrue(names.contains("height"));
        }

        @Test
        @DisplayName("should return empty set when no fields are registered")
        void shouldReturnEmptySetWhenNoFields() {
            PresetSchema schema = new TestSchema(Map.of());

            Set<String> names = schema.getFieldNames();
            assertTrue(names.isEmpty());
        }

        @Test
        @DisplayName("should return unmodifiable set")
        void shouldReturnUnmodifiableSet() {
            PresetSchema schema = new TestSchema(Map.of("dpmm", def("8dpmm")));

            Set<String> names = schema.getFieldNames();
            assertThrows(UnsupportedOperationException.class, () -> names.add("newField"));
        }
    }

    @Nested
    @DisplayName("getDefaultValue")
    class GetDefaultValue {

        @Test
        @DisplayName("should return default value for known field")
        void shouldReturnDefaultValueForKnownField() {
            PresetSchema schema = new TestSchema(Map.of("dpmm", def("8dpmm")));

            assertEquals("8dpmm", schema.getDefaultValue("dpmm"));
        }

        @Test
        @DisplayName("should return null for unknown field")
        void shouldReturnNullForUnknownField() {
            PresetSchema schema = new TestSchema(Map.of("dpmm", def("8dpmm")));

            assertNull(schema.getDefaultValue("nonexistent"));
        }

        @Test
        @DisplayName("should return correct default value types")
        void shouldReturnCorrectDefaultValueTypes() {
            PresetSchema schema = new TestSchema(Map.of(
                    "stringField", def("hello"),
                    "numericField", def(5.9),
                    "intField", def(42)));

            assertEquals("hello", schema.getDefaultValue("stringField"));
            assertEquals(5.9, schema.getDefaultValue("numericField"));
            assertEquals(42, schema.getDefaultValue("intField"));
        }

        @Test
        @DisplayName("should support null default values")
        void shouldSupportNullDefaultValues() {
            PresetSchema schema = new TestSchema(Map.of("optField", def(null)));

            assertNull(schema.getDefaultValue("optField"));
        }
    }

    @Nested
    @DisplayName("getRequiredFieldDefinitions")
    class GetRequiredFieldDefinitions {

        @Test
        @DisplayName("should return copy of required fields with correct values")
        void shouldReturnCopyOfRequiredFields() {
            TestSchema schema = new TestSchema(Map.of(
                    "dpmm", def("8dpmm"),
                    "width", def(5.9)));

            Map<String, AbstractPresetSchema.FieldDefinition> defs = schema.getRequiredFieldDefinitions();
            assertEquals(2, defs.size());
            assertEquals("8dpmm", defs.get("dpmm").defaultValue());
            assertEquals(5.9, defs.get("width").defaultValue());
        }

        @Test
        @DisplayName("should return unmodifiable map")
        void shouldReturnUnmodifiableMap() {
            TestSchema schema = new TestSchema(Map.of("dpmm", def("8dpmm")));

            Map<String, AbstractPresetSchema.FieldDefinition> defs = schema.getRequiredFieldDefinitions();
            assertThrows(UnsupportedOperationException.class,
                    () -> defs.put("newField", def("value")));
        }
    }

    @Nested
    @DisplayName("FieldDefinition")
    class FieldDefinitionTests {

        @Test
        @DisplayName("should store default value")
        void shouldStoreDefaultValue() {
            AbstractPresetSchema.FieldDefinition fd = AbstractPresetSchema.FieldDefinition.of("test");
            assertEquals("test", fd.defaultValue());
        }

        @Test
        @DisplayName("should support null default value")
        void shouldSupportNullDefault() {
            AbstractPresetSchema.FieldDefinition fd = AbstractPresetSchema.FieldDefinition.of(null);
            assertNull(fd.defaultValue());
        }

        @Test
        @DisplayName("should be a record with proper equality")
        void shouldBeRecordWithProperEquality() {
            var fd1 = AbstractPresetSchema.FieldDefinition.of("value");
            var fd2 = AbstractPresetSchema.FieldDefinition.of("value");
            var fd3 = AbstractPresetSchema.FieldDefinition.of("other");

            assertEquals(fd1, fd2);
            assertNotEquals(fd1, fd3);
        }
    }
}