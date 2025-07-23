package com.rhizomind.quickapp;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.rhizomind.quickapp.model.Manifest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Commons {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
      .disable(FAIL_ON_UNKNOWN_PROPERTIES);

  public static Manifest loadManifest(File file) throws IOException {
    try (var inputStream = new FileInputStream(file)) {
      return loadManifest(inputStream);
    }
  }

  public static Manifest loadManifest(InputStream inputStream) throws IOException {
    return OBJECT_MAPPER.readValue(inputStream, Manifest.class);
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
