package io.nthcristian.zplrdr;

import static org.junit.jupiter.api.Assertions.*;

import io.nthcristian.zplrdr.contract.PresetSchema;
import io.nthcristian.zplrdr.error.PresetSchemaException;
import io.nthcristian.zplrdr.error.PresetServiceException;
import io.nthcristian.zplrdr.preset.Preset;
import io.nthcristian.zplrdr.preset.PresetRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@DisplayName("PresetService")
class PresetServiceTest {

    @TempDir
    Path tempDir;

    private PresetRepository repository;
    private LabelarySchemaStub schema;
    private PresetService service;

    static class LabelarySchemaStub implements PresetSchema {
        @Override
        public void validate(Map<String, String> fields) throws PresetSchemaException {
            if (!fields.containsKey("dpmm")) {
                throw new PresetSchemaException("Required field 'dpmm' is missing");
            }
            if (!fields.containsKey("width")) {
                throw new PresetSchemaException("Required field 'width' is missing");
            }
            if (!fields.containsKey("height")) {
                throw new PresetSchemaException("Required field 'height' is missing");
            }
        }

        @Override
        public Object getDefaultValue(String field) {
            return switch (field) {
                case "dpmm" -> "8dpmm";
                case "width" -> 5.9;
                case "height" -> 3.9;
                default -> null;
            };
        }

        @Override
        public Set<String> getFieldNames() {
            return Set.of("dpmm", "width", "height");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        repository = new PresetRepository(tempDir.toString());
        schema = new LabelarySchemaStub();
        service = new PresetService(schema, repository);
    }

    @Nested
    @DisplayName("createPreset")
    class CreatePreset {

        @Test
        @DisplayName("should create a new preset with default values")
        void shouldCreateNewPresetWithDefaultValues() throws Exception {
            Preset preset = service.createPreset("label1");

            assertEquals("label1", preset.name());
            assertEquals("8dpmm", preset.getProperty("dpmm"));
            assertEquals("5.9", preset.getProperty("width"));
            assertEquals("3.9", preset.getProperty("height"));
        }

        @Test
        @DisplayName("should return structurally correct Preset without persisting to cache")
        void shouldReturnValidPresetWithoutPersisting() throws Exception {
            Preset preset = service.createPreset("label1");

            // createPreset does NOT add to activePresets (known limitation)
            assertNull(service.getPreset("label1"),
                    "createPreset returns a Preset but does not add it to activePresets until savePreset is called");

            // However the returned Preset is valid
            assertEquals("label1", preset.name());
        }

        @Test
        @DisplayName("should become retrievable after savePreset")
        void shouldBecomeRetrievableAfterSave() throws Exception {
            Preset created = service.createPreset("label1");
            Preset saved = service.savePreset(created);

            Preset retrieved = service.getPreset("label1");
            assertEquals(saved, retrieved);
        }

        @Test
        @DisplayName("should create multiple distinct presets and save them")
        void shouldCreateMultipleDistinctPresets() throws Exception {
            Preset p1 = service.createPreset("label1");
            Preset p2 = service.createPreset("label2");

            service.savePreset(p1);
            service.savePreset(p2);

            assertEquals("label1", service.getPreset("label1").name());
            assertEquals("label2", service.getPreset("label2").name());
            assertNotEquals(service.getPreset("label1"), service.getPreset("label2"));
        }
    }

    @Nested
    @DisplayName("getPreset")
    class GetPreset {

        @Test
        @DisplayName("should return null for non-existent preset")
        void shouldReturnNullForNonExistent() {
            assertNull(service.getPreset("nonexistent"));
        }
    }

    @Nested
    @DisplayName("savePreset")
    class SavePreset {

        @Test
        @DisplayName("should save and retrieve a preset by name")
        void shouldSaveAndRetrievePreset() throws Exception {
            Preset preset = service.createPreset("label1");
            service.savePreset(preset);

            Preset retrieved = service.getPreset("label1");
            assertNotNull(retrieved);
            assertEquals("label1", retrieved.name());
        }

        @Test
        @DisplayName("should update existing preset on save")
        void shouldUpdateExistingPresetOnSave() throws Exception {
            Preset created = service.createPreset("label1");
            service.savePreset(created);

            Preset modified = new Preset("label1",
                    Map.of("dpmm", "12dpmm", "width", "8.0", "height", "6.0"));
            service.savePreset(modified);

            Preset retrieved = service.getPreset("label1");
            assertEquals("12dpmm", retrieved.getProperty("dpmm"));
            assertEquals("8.0", retrieved.getProperty("width"));
            assertEquals("6.0", retrieved.getProperty("height"));
        }

        @Test
        @DisplayName("should reject preset with invalid fields")
        void shouldRejectPresetWithInvalidFields() {
            Preset invalid = new Preset("label1",
                    Map.of("dpmm", "8dpmm"));
            // missing width and height

            PresetServiceException ex = assertThrows(PresetServiceException.class,
                    () -> service.savePreset(invalid));
            assertTrue(ex.getMessage().contains("Could not update/create preset"));
            assertInstanceOf(PresetSchemaException.class, ex.getCause());
        }

        @Test
        @DisplayName("should persist to disk and survive new service instance")
        void shouldPersistToDiskAndSurviveNewServiceInstance() throws Exception {
            Preset preset = service.createPreset("label1");
            service.savePreset(preset);

            // Create a fresh service backed by the same temp directory
            PresetService freshService = new PresetService(schema, repository);

            Preset loaded = freshService.getPreset("label1");
            assertNotNull(loaded);
            assertEquals("label1", loaded.name());
            assertEquals("8dpmm", loaded.getProperty("dpmm"));
        }
    }

    @Nested
    @DisplayName("deletePreset")
    class DeletePreset {

        @Test
        @DisplayName("should delete an existing saved preset")
        void shouldDeleteExistingSavedPreset() throws Exception {
            Preset preset = service.createPreset("label1");
            service.savePreset(preset);
            assertNotNull(service.getPreset("label1"));

            service.deletePreset("label1");

            assertNull(service.getPreset("label1"));
        }

        @Test
        @DisplayName("should throw when deleting non-existent preset")
        void shouldThrowWhenDeletingNonExistent() {
            PresetServiceException ex = assertThrows(PresetServiceException.class,
                    () -> service.deletePreset("nonexistent"));
            assertTrue(ex.getMessage().contains("No preset found"));
            assertTrue(ex.getMessage().contains("nonexistent"));
        }

        @Test
        @DisplayName("should throw when deleting already-deleted preset")
        void shouldThrowWhenDeletingAlreadyDeleted() throws Exception {
            Preset preset = service.createPreset("label1");
            service.savePreset(preset);
            service.deletePreset("label1");

            assertThrows(PresetServiceException.class, () -> service.deletePreset("label1"));
        }
    }

    @Nested
    @DisplayName("cache initialization from repository")
    class CacheInit {

        @Test
        @DisplayName("should load presets from repository on startup")
        void shouldLoadPresetsFromRepositoryOnStartup() throws Exception {
            Map<String, String> fields = Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9");
            repository.save("label1", fields);

            PresetService freshService = new PresetService(schema, repository);

            Preset loaded = freshService.getPreset("label1");
            assertNotNull(loaded);
            assertEquals("8dpmm", loaded.getProperty("dpmm"));
        }

        @Test
        @DisplayName("should handle empty repository on startup gracefully")
        void shouldHandleEmptyRepositoryOnStartup() {
            assertDoesNotThrow(() -> new PresetService(schema, repository));
        }

        @Test
        @DisplayName("should skip invalid presets in repository during init")
        void shouldSkipInvalidPresetsDuringInit() throws Exception {
            Map<String, String> invalidFields = new HashMap<>();
            invalidFields.put("dpmm", "8dpmm");
            invalidFields.put("width", "5.9");
            // height intentionally missing
            repository.save("invalid-preset", invalidFields);

            Map<String, String> validFields = Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9");
            repository.save("valid-preset", validFields);

            PresetService freshService = new PresetService(schema, repository);

            assertNull(freshService.getPreset("invalid-preset"));
            assertNotNull(freshService.getPreset("valid-preset"));
        }
    }
}