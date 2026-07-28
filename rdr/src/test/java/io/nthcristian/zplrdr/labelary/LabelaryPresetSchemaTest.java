package io.nthcristian.zplrdr.labelary;

import static org.junit.jupiter.api.Assertions.*;

import io.nthcristian.zplrdr.error.PresetSchemaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@DisplayName("LabelaryPresetSchema")
class LabelaryPresetSchemaTest {

    private LabelaryPresetSchema schema;

    @BeforeEach
    void setUp() {
        schema = new LabelaryPresetSchema();
    }

    @Nested
    @DisplayName("construction and configuration")
    class Construction {

        @Test
        @DisplayName("should have three required fields: dpmm, width, height")
        void shouldHaveThreeRequiredFields() {
            Set<String> fieldNames = schema.getFieldNames();
            assertEquals(3, fieldNames.size());
            assertTrue(fieldNames.contains("dpmm"));
            assertTrue(fieldNames.contains("width"));
            assertTrue(fieldNames.contains("height"));
        }

        @Test
        @DisplayName("should have default values for all fields")
        void shouldHaveDefaultValues() {
            assertEquals("8dpmm", schema.getDefaultValue("dpmm"));
            assertEquals(3.9, schema.getDefaultValue("width"));
            assertEquals(5.9, schema.getDefaultValue("height"));
        }
    }

    @Nested
    @DisplayName("validate — happy path")
    class ValidateHappyPath {

        @Test
        @DisplayName("should accept valid fields")
        void shouldAcceptValidFields() {
            Map<String, String> fields = Map.of(
                    "dpmm", "8dpmm",
                    "width", "5.9",
                    "height", "3.9");

            assertDoesNotThrow(() -> schema.validate(fields));
        }

        @Test
        @DisplayName("should accept minimum dpmm value")
        void shouldAcceptMinimumDpmm() {
            Map<String, String> fields = Map.of(
                    "dpmm", "1dpmm",
                    "width", "0.1",
                    "height", "0.1");

            assertDoesNotThrow(() -> schema.validate(fields));
        }

        @Test
        @DisplayName("should accept multi-digit dpmm value")
        void shouldAcceptMultiDigitDpmm() {
            Map<String, String> fields = Map.of(
                    "dpmm", "24dpmm",
                    "width", "10",
                    "height", "20");

            assertDoesNotThrow(() -> schema.validate(fields));
        }

        @Test
        @DisplayName("should accept integer width and height")
        void shouldAcceptIntegerWidthAndHeight() {
            Map<String, String> fields = Map.of(
                    "dpmm", "8dpmm",
                    "width", "5",
                    "height", "3");

            assertDoesNotThrow(() -> schema.validate(fields));
        }

        @Test
        @DisplayName("should accept decimal width and height")
        void shouldAcceptDecimalWidthAndHeight() {
            Map<String, String> fields = Map.of(
                    "dpmm", "8dpmm",
                    "width", "8.5",
                    "height", "11.0");

            assertDoesNotThrow(() -> schema.validate(fields));
        }
    }

    @Nested
    @DisplayName("validate — missing fields")
    class ValidateMissingFields {

        @Test
        @DisplayName("should reject when dpmm is missing")
        void shouldRejectMissingDpmm() {
            Map<String, String> fields = new HashMap<>();
            fields.put("width", "5.9");
            fields.put("height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("dpmm"));
            assertTrue(ex.getMessage().contains("missing"));
        }

        @Test
        @DisplayName("should reject when width is missing")
        void shouldRejectMissingWidth() {
            Map<String, String> fields = new HashMap<>();
            fields.put("dpmm", "8dpmm");
            fields.put("height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("width"));
            assertTrue(ex.getMessage().contains("missing"));
        }

        @Test
        @DisplayName("should reject when height is missing")
        void shouldRejectMissingHeight() {
            Map<String, String> fields = new HashMap<>();
            fields.put("dpmm", "8dpmm");
            fields.put("width", "5.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("height"));
            assertTrue(ex.getMessage().contains("missing"));
        }

        @Test
        @DisplayName("should reject empty map")
        void shouldRejectEmptyMap() {
            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(Map.of()));
            assertTrue(ex.getMessage().contains("missing"));
        }
    }

    @Nested
    @DisplayName("validate — blank fields")
    class ValidateBlankFields {

        @Test
        @DisplayName("should reject blank dpmm")
        void shouldRejectBlankDpmm() {
            Map<String, String> fields = Map.of(
                    "dpmm", "   ",
                    "width", "5.9",
                    "height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("dpmm"));
            assertTrue(ex.getMessage().toLowerCase().contains("blank"));
        }

        @Test
        @DisplayName("should reject empty string dpmm")
        void shouldRejectEmptyStringDpmm() {
            Map<String, String> fields = Map.of(
                    "dpmm", "",
                    "width", "5.9",
                    "height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("dpmm"));
            assertTrue(ex.getMessage().toLowerCase().contains("blank"));
        }
    }

    @Nested
    @DisplayName("validate — invalid dpmm format")
    class ValidateInvalidDpmm {

        @Test
        @DisplayName("should reject dpmm without 'dpmm' suffix")
        void shouldRejectDpmmWithoutSuffix() {
            Map<String, String> fields = Map.of(
                    "dpmm", "8",
                    "width", "5.9",
                    "height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("<number>dpmm"));
        }

        @Test
        @DisplayName("should reject dpmm with decimal")
        void shouldRejectDpmmWithDecimal() {
            Map<String, String> fields = Map.of(
                    "dpmm", "8.5dpmm",
                    "width", "5.9",
                    "height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("<number>dpmm"));
        }

        @Test
        @DisplayName("should reject dpmm with letters mixed")
        void shouldRejectDpmmWithLetters() {
            Map<String, String> fields = Map.of(
                    "dpmm", "abc8dpmm",
                    "width", "5.9",
                    "height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("<number>dpmm"));
        }
    }

    @Nested
    @DisplayName("validate — invalid width/height format")
    class ValidateInvalidDimensions {

        @Test
        @DisplayName("should reject non-numeric width")
        void shouldRejectNonNumericWidth() {
            Map<String, String> fields = Map.of(
                    "dpmm", "8dpmm",
                    "width", "abc",
                    "height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("width"));
            assertTrue(ex.getMessage().contains("numeric"));
        }

        @Test
        @DisplayName("should reject non-numeric height")
        void shouldRejectNonNumericHeight() {
            Map<String, String> fields = Map.of(
                    "dpmm", "8dpmm",
                    "width", "5.9",
                    "height", "abc");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("height"));
            assertTrue(ex.getMessage().contains("numeric"));
        }

        @Test
        @DisplayName("should reject width with letters attached")
        void shouldRejectWidthWithLetters() {
            Map<String, String> fields = Map.of(
                    "dpmm", "8dpmm",
                    "width", "5in",
                    "height", "3.9");

            PresetSchemaException ex = assertThrows(PresetSchemaException.class,
                    () -> schema.validate(fields));
            assertTrue(ex.getMessage().contains("numeric"));
        }
    }
}