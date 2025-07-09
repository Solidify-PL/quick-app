package com.rhizomind.quickapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Commons {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    public static Manifest loadManifest(InputStream inputStream) throws IOException {
        return new ObjectMapper(new YAMLFactory()).readValue(inputStream, Manifest.class);
    }

    public static Manifest loadManifest(File file) throws IOException {
        return new ObjectMapper(new YAMLFactory()).readValue(file, Manifest.class);
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
