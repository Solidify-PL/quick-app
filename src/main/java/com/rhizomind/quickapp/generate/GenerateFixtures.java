package com.rhizomind.quickapp.generate;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;
import static com.rhizomind.quickapp.Commons.loadManifest;
import static com.rhizomind.quickapp.Commons.loadMapParameters;
import static com.rhizomind.quickapp.common.Process.execute;

import com.rhizomind.quickapp.Manifest;
import com.rhizomind.quickapp.common.Joiner;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class GenerateFixtures {

    public static Manifest doGenerate(File templateDir, File outputDir, boolean forceRewrite,
            File valuesFile, Map<String, String> valuesOverride)
            throws IOException, InterruptedException {

        var manifest = loadManifest(new File(templateDir, "manifest.yaml"));
        var values = merge(
                valuesOverride,
                merge(
                        loadMapParameters(valuesFile),
                        loadMapParameters(new File(templateDir, manifest.getValues().getDefaults()))
                )
        );
        System.out.println("Generating " + templateDir);
        System.out.println("Using following values:");
        System.out.println(OBJECT_MAPPER.writeValueAsString(values));

        var tempValuesFile = Files.createTempFile("quickapp", "values").toFile();
        OBJECT_MAPPER.writeValue(tempValuesFile, values);
        var command = "docker run --rm -u $(id -u):$(id -g) "
                + " -v " + new File(templateDir, "files").getAbsolutePath()
                + ":/tmp/quickapp/input:ro "
                + " -v " + tempValuesFile.getAbsolutePath()
                + ":/tmp/quickapp/values.yaml:ro "
                + " -v " + outputDir.getAbsolutePath() + ":/tmp/quickapp/output "
                + " " + manifest.getGenerator().getImage()
                + " --input=/tmp/quickapp/input "
                + " --values=/tmp/quickapp/values.yaml "
                + " --output=/tmp/quickapp/output "
                + (forceRewrite ? " --force " : "")
                + Joiner.join(manifest.getGenerator().getArgs());

        if (execute(command) != 0) {
            throw new RuntimeException("Error executing generator: " + command);
        }
        return manifest;
    }

    public static Map<String, String> merge(Map<String, String> values,
            Map<String, String> defaults) {
        HashMap<String, String> result = new HashMap<>();
        result.putAll(defaults);
        result.putAll(values);
        return result;
    }
}
