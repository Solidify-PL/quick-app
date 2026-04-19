package com.rhizomind.quickapp;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.rhizomind.quickapp.model.Manifest;
import com.rhizomind.quickapp.model.ManifestLoadException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class Commons {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
            .disable(FAIL_ON_UNKNOWN_PROPERTIES);

    public static Manifest loadManifest(File file) throws IOException {
        try (var inputStream = new FileInputStream(file)) {
            return loadManifest(inputStream, file.getAbsolutePath());
        }
    }

    public static Manifest loadManifest(InputStream inputStream) throws IOException {
        return loadManifest(inputStream, null);
    }

    public static Manifest loadManifest(byte[] content, String source) throws IOException {
        return loadManifest(new ByteArrayInputStream(content), source);
    }

    public static Manifest loadManifest(InputStream inputStream, String source) throws IOException {
        try {
            return OBJECT_MAPPER.readValue(inputStream, Manifest.class);
        } catch (JsonParseException e) {
            JsonLocation loc = e.getLocation();
            throw new ManifestLoadException(
                    ManifestLoadException.Kind.INVALID_YAML,
                    source,
                    loc != null ? loc.getLineNr() : -1,
                    loc != null ? loc.getColumnNr() : -1,
                    null,
                    e.getOriginalMessage(),
                    e);
        } catch (JsonMappingException e) {
            JsonLocation loc = e.getLocation();
            throw new ManifestLoadException(
                    ManifestLoadException.Kind.INVALID_SCHEMA,
                    source,
                    loc != null ? loc.getLineNr() : -1,
                    loc != null ? loc.getColumnNr() : -1,
                    formatFieldPath(e),
                    e.getOriginalMessage(),
                    e);
        }
    }

    private static String formatFieldPath(JsonMappingException e) {
        if (e.getPath() == null || e.getPath().isEmpty()) {
            return null;
        }
        return e.getPath().stream()
                .map(ref -> ref.getFieldName() != null
                        ? ref.getFieldName()
                        : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."));
    }

    public static JsonNode loadSchema(File schemaFile) throws IOException {
        if (schemaFile == null) {

        }
        if (!schemaFile.exists()) {
            throw new RuntimeException(
                    "File '" + schemaFile.getAbsolutePath() + "' does not exist");
        }
        return OBJECT_MAPPER.readTree(schemaFile);
    }

    public static Map<String, String> loadMapParameters(File valuesFile) throws IOException {
        if (valuesFile == null) {
            return new HashMap<>();
        }
        if (!valuesFile.exists()) {
            throw new RuntimeException(
                    "File '" + valuesFile.getAbsolutePath() + "' does not exist");
        }
        if (valuesFile.length() == 0) {
            return new HashMap<>();
        }
        return OBJECT_MAPPER.readValue(valuesFile,
                new TypeReference<Map<String, String>>() {
                });
    }
}
