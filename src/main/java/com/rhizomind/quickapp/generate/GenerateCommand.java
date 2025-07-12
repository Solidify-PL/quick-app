package com.rhizomind.quickapp.generate;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;
import static com.rhizomind.quickapp.Commons.loadManifest;
import static com.rhizomind.quickapp.Commons.loadMapParameters;
import static com.rhizomind.quickapp.generate.TemplateRef.isTemplateRef;
import static com.rhizomind.quickapp.generate.TemplateRef.parse;
import static com.rhizomind.quickapp.common.Compress.extractTarGz;
import static com.rhizomind.quickapp.common.Process.execute;
import static com.rhizomind.quickapp.cache.Fixtures.getTemplatePackageFile;

import com.rhizomind.quickapp.common.Joiner;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * quick-app --values=values.yaml -value param1=value1 --template=./spring --output=./output
 * --force
 */
@Command(
        name = "generate",
        mixinStandardHelpOptions = true,
        description = "Generates source code based on a template"
)
public class GenerateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "template directory or name (<repoName>/<templateName>[:version])")
    private String templateDirOrName;

    @Option(names = {"-v", "--values"}, description = "Values yaml ")
    private File valuesFile;

    @Option(names = {"--value"},
            description = "Parametry nadpisujace wartosci z pliku values --value <nazwa>=<wartość>",
            paramLabel = "<nazwa>=<wartość>")
    private Map<String, String> values = new HashMap<>();

    @Option(names = {"-o",
            "--output"}, description = "Output directory (Workdir by default) ", required = true)
    private File outputDir;

    @Option(names = {"-f", "--force"}, description = "Rewrite output", defaultValue = "false")
    private boolean forceRewrite;

    @Override
    public Integer call() throws Exception {
        if (outputDir.exists() && !outputDir.isDirectory()) {
            throw new IllegalArgumentException(
                    "Output directory '" + outputDir.getAbsolutePath() + "' is not a directory");
        } else {
            outputDir.mkdirs();
        }
        var templateDir = prepareTemplateDir(templateDirOrName);

        var manifest = loadManifest(new File(templateDir, "manifest.yaml"));
        var values = merge(
                this.values,
                merge(
                        loadMapParameters(this.valuesFile),
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

        return execute(command);
    }

    private File prepareTemplateDir(String templateDirOrName1) throws IOException {
        if (isTemplateRef(templateDirOrName1)) {
            var templateRef = parse(templateDirOrName1);
            var templatePackageFile = getTemplatePackageFile(templateRef);

            var tmpExtractionDir = extractTarGz(templatePackageFile);
            return new File(tmpExtractionDir, templateRef.getName());
        } else {
            return new File(templateDirOrName1);
        }
    }

    private Map<String, String> merge(Map<String, String> values, Map<String, String> defaults) {
        HashMap<String, String> result = new HashMap<>();
        result.putAll(defaults);
        result.putAll(values);
        return result;
    }

}
