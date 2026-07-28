package io.nthcristian.zplrdr.preset;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nthcristian.zplrdr.error.PresetStorageException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PresetRepository {
    private static final String FILE_EXTENSION = ".json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    private final Path storagePath;
    private final Map<String, Map<String, String>> cache;

    public PresetRepository(@NotBlank String storageDirectory) {
        this.storagePath = Path.of(storageDirectory);
        this.cache = new HashMap<>();
    }

    public Map<String, String> findByName(@NotBlank String presetName) throws PresetStorageException {
        Map<String, String> cached = cache.get(presetName);
        if (cached != null) {
            return Collections.unmodifiableMap(cached);
        }

        Path filePath = resolveFilePath(presetName);
        String raw;
        try {
            raw = Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PresetStorageException("Failed to read preset file: " + filePath, e);
        }

        Map<String, String> fields;
        try {
            fields = objectMapper.readValue(raw, MAP_TYPE);
        } catch (IOException e) {
            throw new PresetStorageException("Failed to parse JSON for preset: " + presetName, e);
        }

        cache.put(presetName, fields);
        return Collections.unmodifiableMap(fields);
    }

    public Map<String, Map<String, String>> findAll() throws PresetStorageException {
        ensureStorageDirectory();
        Map<String, Map<String, String>> result = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storagePath, "*" + FILE_EXTENSION)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                String presetName = fileName.substring(0, fileName.length() - FILE_EXTENSION.length());
                result.put(presetName, findByName(presetName));
            }
        } catch (NoSuchFileException e) {
            // directory doesn't exist yet — return empty map
        } catch (IOException e) {
            throw new PresetStorageException("Failed to list preset files", e);
        }

        return result;
    }

    public void save(@NotBlank String presetName, @NotNull Map<String, String> fields) throws PresetStorageException {
        ensureStorageDirectory();

        String json;
        try {
            json = objectMapper.writeValueAsString(fields);
        } catch (IOException e) {
            throw new PresetStorageException("Failed to serialize preset: " + presetName, e);
        }

        Path filePath = resolveFilePath(presetName);
        try {
            Files.writeString(filePath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PresetStorageException("Failed to write preset file: " + filePath, e);
        }

        cache.put(presetName, Map.copyOf(fields));
    }

    public void deleteByName(@NotBlank String presetName) throws PresetStorageException {
        cache.remove(presetName);
        Path filePath = resolveFilePath(presetName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new PresetStorageException("Failed to delete preset file: " + filePath, e);
        }
    }

    private Path resolveFilePath(String presetName) {
        return storagePath.resolve(presetName + FILE_EXTENSION);
    }

    private void ensureStorageDirectory() throws PresetStorageException {
        try {
            if (!Files.isDirectory(storagePath)) {
                Files.createDirectories(storagePath);
            }
        } catch (IOException e) {
            throw new PresetStorageException("Failed to create store directory: " + storagePath, e);
        }
    }
}