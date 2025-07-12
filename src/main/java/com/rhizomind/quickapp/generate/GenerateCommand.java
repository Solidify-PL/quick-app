package com.rhizomind.quickapp.generate;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

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

        GenerateFixtures.doGenerate(templateDirOrName, outputDir, forceRewrite, this.valuesFile,
                this.values);

        return 0;
    }

}
