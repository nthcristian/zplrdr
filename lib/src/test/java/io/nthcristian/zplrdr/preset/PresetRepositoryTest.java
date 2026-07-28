package io.nthcristian.zplrdr.preset;

import static org.junit.jupiter.api.Assertions.*;

import io.nthcristian.zplrdr.error.PresetStorageException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@DisplayName("PresetRepository")
class PresetRepositoryTest {

    @TempDir
    Path tempDir;

    private PresetRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PresetRepository(tempDir.toString());
    }

    @Nested
    @DisplayName("save and findByName")
    class SaveAndFind {

        @Test
        @DisplayName("should save and retrieve fields")
        void shouldSaveAndRetrieveFields() throws Exception {
            Map<String, String> fields = Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9");
            repository.save("test-preset", fields);

            Map<String, String> loaded = repository.findByName("test-preset");
            assertEquals(fields, loaded);
        }

        @Test
        @DisplayName("should overwrite existing preset on save")
        void shouldOverwriteExistingPresetOnSave() throws Exception {
            Map<String, String> original = Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9");
            repository.save("test-preset", original);

            Map<String, String> updated = Map.of("dpmm", "12dpmm", "width", "8.0", "height", "6.0");
            repository.save("test-preset", updated);

            Map<String, String> loaded = repository.findByName("test-preset");
            assertEquals(updated, loaded);
        }

        @Test
        @DisplayName("should store as JSON file on disk")
        void shouldStoreAsJsonFileOnDisk() throws Exception {
            Map<String, String> fields = Map.of("dpmm", "8dpmm");
            repository.save("disk-preset", fields);

            Path filePath = tempDir.resolve("disk-preset.json");
            assertTrue(Files.exists(filePath));

            String raw = Files.readString(filePath);
            assertTrue(raw.contains("dpmm"));
            assertTrue(raw.contains("8dpmm"));
        }

        @Test
        @DisplayName("should return unmodifiable map from findByName")
        void shouldReturnUnmodifiableMapFromFindByName() throws Exception {
            Map<String, String> fields = Map.of("dpmm", "8dpmm");
            repository.save("test-preset", fields);

            Map<String, String> loaded = repository.findByName("test-preset");
            assertThrows(UnsupportedOperationException.class,
                    () -> loaded.put("dpmm", "12dpmm"));
        }

        @Test
        @DisplayName("should cache result after first lookup")
        void shouldCacheResultAfterFirstLookup() throws Exception {
            Map<String, String> fields = Map.of("dpmm", "8dpmm");
            repository.save("cached-preset", fields);

            Map<String, String> first = repository.findByName("cached-preset");
            Map<String, String> second = repository.findByName("cached-preset");

            // Same values
            assertEquals(first, second);
        }

        @Test
        @DisplayName("should throw PresetStorageException on read failure")
        void shouldThrowOnReadFailure() {
            assertThrows(PresetStorageException.class,
                    () -> repository.findByName("nonexistent"));
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return empty map when no presets exist")
        void shouldReturnEmptyMapWhenNoPresets() throws Exception {
            Map<String, Map<String, String>> all = repository.findAll();
            assertTrue(all.isEmpty());
        }

        @Test
        @DisplayName("should return all saved presets")
        void shouldReturnAllSavedPresets() throws Exception {
            Map<String, String> fields1 = Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9");
            Map<String, String> fields2 = Map.of("dpmm", "12dpmm", "width", "8.0", "height", "6.0");
            repository.save("preset1", fields1);
            repository.save("preset2", fields2);

            Map<String, Map<String, String>> all = repository.findAll();
            assertEquals(2, all.size());
            assertEquals(fields1, all.get("preset1"));
            assertEquals(fields2, all.get("preset2"));
        }
    }

    @Nested
    @DisplayName("deleteByName")
    class DeleteByName {

        @Test
        @DisplayName("should delete existing preset from disk and cache")
        void shouldDeleteExistingPreset() throws Exception {
            Map<String, String> fields = Map.of("dpmm", "8dpmm");
            repository.save("to-delete", fields);

            repository.deleteByName("to-delete");

            Path filePath = tempDir.resolve("to-delete.json");
            assertFalse(Files.exists(filePath));
            assertThrows(PresetStorageException.class,
                    () -> repository.findByName("to-delete"));
        }

        @Test
        @DisplayName("should not throw when deleting non-existent preset")
        void shouldNotThrowWhenDeletingNonExistent() {
            assertDoesNotThrow(() -> repository.deleteByName("no-such-preset"));
        }
    }

    @Nested
    @DisplayName("auto-create directory")
    class AutoCreateDirectory {

        @Test
        @DisplayName("should create storage directory when saving if it does not exist")
        void shouldCreateStorageDirectoryWhenSaving() throws Exception {
            Path nestedDir = tempDir.resolve("nested").resolve("dir");
            PresetRepository nestedRepo = new PresetRepository(nestedDir.toString());

            assertFalse(Files.exists(nestedDir));

            Map<String, String> fields = Map.of("dpmm", "8dpmm");
            nestedRepo.save("test-preset", fields);

            assertTrue(Files.isDirectory(nestedDir));
        }
    }
}