package com.rhizomind.quickapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class Commons {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
            .disable(FAIL_ON_UNKNOWN_PROPERTIES);

    public static Manifest loadManifest(File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, Manifest.class);
    }

}
