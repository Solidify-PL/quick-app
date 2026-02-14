package com.rhizomind.quickapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rhizomind.quickapp.model.Manifest;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;
import static com.rhizomind.quickapp.Commons.loadManifest;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@Slf4j
public class Generators {

    public static final MustacheGeneratorConfig DEFAULT_GENERATOR = new MustacheGeneratorConfig(emptySet());

    public static Manifest doGenerate(
            File templateDir, File outputDir, boolean forceRewrite,
            File valuesFile, Map<String, String> valuesOverride)
            throws Exception {

        var manifest = loadManifest(new File(templateDir, "manifest.yaml"));
        var values = merge(
                valuesOverride,
                merge(
                        loadMapParameters(valuesFile),
                        loadMapParameters(new File(templateDir, manifest.getValues().getDefaults()))
                )
        );

        firstNonNull(manifest.getGenerator(), DEFAULT_GENERATOR)
                .createGenerator()
                .generate(new File(templateDir, "files").toPath(), outputDir.toPath(), values);

        return manifest;
    }

    public static Map<String, String> merge(Map<String, String> values,
                                            Map<String, String> defaults) {
        HashMap<String, String> result = new HashMap<>();
        result.putAll(defaults);
        result.putAll(values);
        return result;
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
