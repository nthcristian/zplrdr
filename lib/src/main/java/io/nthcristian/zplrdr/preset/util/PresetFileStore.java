package io.nthcristian.zplrdr.preset.util;

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

import io.nthcristian.zplrdr.error.PresetFileStoreException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PresetFileStore {
    private static final String FILE_EXTENSION = ".json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    private final Path storeFolder;
    private final Map<String, Map<String, String>> cache;

    public PresetFileStore(@NotBlank String storeFolderPath) {
        this.storeFolder = Path.of(storeFolderPath);
        this.cache = new HashMap<>();
    }

    public Map<String, String> load(@NotBlank String presetName) throws PresetFileStoreException {
        Map<String, String> cached = cache.get(presetName);
        if (cached != null) {
            return Collections.unmodifiableMap(cached);
        }

        Path filePath = resolveFile(presetName);
        String raw;
        try {
            raw = Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PresetFileStoreException("Failed to read preset file: " + filePath, e);
        }

        Map<String, String> fields;
        try {
            fields = objectMapper.readValue(raw, MAP_TYPE);
        } catch (IOException e) {
            throw new PresetFileStoreException("Failed to parse JSON for preset: " + presetName, e);
        }

        cache.put(presetName, fields);
        return Collections.unmodifiableMap(fields);
    }

    public Map<String, Map<String, String>> loadAll() throws PresetFileStoreException {
        ensureDirectory();
        Map<String, Map<String, String>> result = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storeFolder, "*" + FILE_EXTENSION)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                String presetName = fileName.substring(0, fileName.length() - FILE_EXTENSION.length());
                result.put(presetName, load(presetName));
            }
        } catch (NoSuchFileException e) {
            // directory doesn't exist yet — return empty map
        } catch (IOException e) {
            throw new PresetFileStoreException("Failed to list preset files", e);
        }

        return result;
    }

    public void save(@NotBlank String presetName, @NotNull Map<String, String> fields) throws PresetFileStoreException {
        ensureDirectory();

        String json;
        try {
            json = objectMapper.writeValueAsString(fields);
        } catch (IOException e) {
            throw new PresetFileStoreException("Failed to serialize preset: " + presetName, e);
        }

        Path filePath = resolveFile(presetName);
        try {
            Files.writeString(filePath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PresetFileStoreException("Failed to write preset file: " + filePath, e);
        }

        cache.put(presetName, Map.copyOf(fields));
    }

    public void delete(@NotBlank String presetName) throws PresetFileStoreException {
        cache.remove(presetName);
        Path filePath = resolveFile(presetName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new PresetFileStoreException("Failed to delete preset file: " + filePath, e);
        }
    }

    private Path resolveFile(String presetName) {
        return storeFolder.resolve(presetName + FILE_EXTENSION);
    }

    private void ensureDirectory() throws PresetFileStoreException {
        try {
            if (!Files.isDirectory(storeFolder)) {
                Files.createDirectories(storeFolder);
            }
        } catch (IOException e) {
            throw new PresetFileStoreException("Failed to create store directory: " + storeFolder, e);
        }
    }
}