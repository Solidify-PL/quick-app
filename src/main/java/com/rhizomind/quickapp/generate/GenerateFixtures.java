package com.rhizomind.quickapp.generate;

import com.rhizomind.quickapp.Manifest;
import com.rhizomind.quickapp.common.Joiner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static com.rhizomind.quickapp.Commons.*;
import static com.rhizomind.quickapp.cache.Fixtures.getTemplatePackageFile;
import static com.rhizomind.quickapp.common.Compress.extractTemplate;
import static com.rhizomind.quickapp.common.Process.execute;
import static com.rhizomind.quickapp.generate.TemplateRef.isTemplateRef;
import static com.rhizomind.quickapp.generate.TemplateRef.parse;

public class GenerateFixtures {

    public static Manifest doGenerate(String templateDirOrName, File outputDir, boolean forceRewrite,
            File valuesFile, Map<String, String> valuesOverride)
            throws IOException, InterruptedException {

        var templateDir = GenerateFixtures.prepareTemplateDir(templateDirOrName);
        var manifest = loadManifest(new File(templateDir, "manifest.yaml"));
        var values = merge(
                valuesOverride,
                merge(
                        loadMapParameters(valuesFile),
                        loadMapParameters(new File(templateDir, manifest.getValues().getDefaults()))
                )
        );
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

    public static File prepareTemplateDir(String templateDirOrName1) throws IOException {
        if (isTemplateRef(templateDirOrName1)) {
            var templateRef = parse(templateDirOrName1);
            var tmpExtractionDir = extractTemplate(getTemplatePackageFile(templateRef));
            return new File(tmpExtractionDir, templateRef.getName());
        } else {
            return new File(templateDirOrName1);
        }
    }

    public static Map<String, String> merge(Map<String, String> values,
            Map<String, String> defaults) {
        HashMap<String, String> result = new HashMap<>();
        result.putAll(defaults);
        result.putAll(values);
        return result;
    }
}
