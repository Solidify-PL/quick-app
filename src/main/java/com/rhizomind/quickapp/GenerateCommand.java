package com.rhizomind.quickapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * quick-app --values=values.yaml -value param1=value1 --template=./spring --output=./output
 * --force
 */
@Command(
        name = "generate",
        mixinStandardHelpOptions = true,
        description = "Generates source code based on a template"
)
class GenerateCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "template directory or name (<repoName>/<templateName>)")
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
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                throw new IllegalArgumentException(
                        "Output directory '" + outputDir.getAbsolutePath()
                                + "' is not a directory");
            }
        } else {
            outputDir.mkdirs();
        }

        var templateFilesDir = new File(templateDirOrName, "files");
        var manifest = Commons.loadManifest(new FileInputStream(new File(templateDirOrName, "manifest.yaml")));
        var values = Commons.loadMapParameters(this.valuesFile);
        var defaults = Commons.loadMapParameters(new File(templateDirOrName, manifest.getValues().getDefaults()));
        var merge = merge(this.values, merge(values, defaults));

        var tempValuesFile = Files.createTempFile("quickapp", "values").toFile();

        Commons.OBJECT_MAPPER.writeValue(tempValuesFile, merge);
        System.out.println("Using following values:");
        Commons.OBJECT_MAPPER.writeValue(System.out, merge);
        System.out.println();
        System.out.flush();

        var command = "docker run --rm -u $(id -u):$(id -g) "
                + " -v " + templateFilesDir.getAbsolutePath() + ":/tmp/quickapp/input:ro "
                + " -v " + tempValuesFile.getAbsolutePath()
                + ":/tmp/quickapp/values.yaml:ro "
                + " -v " + outputDir.getAbsolutePath() + ":/tmp/quickapp/output "
                + " " + manifest.getEngine().getImage()
                + " --input=/tmp/quickapp/input "
                + " --values=/tmp/quickapp/values.yaml "
                + " --output=/tmp/quickapp/output "
                + (forceRewrite ? " --force " : "")
                + join(manifest.getEngine().getArgs());

        return execute(command);
    }

    private String join(List<String> args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        return builder.toString();
    }

    private Integer execute(String command) throws InterruptedException, IOException {
        System.out.println("Executing command: " + command);
        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
        Process process = builder.start();

        // stdout
        Thread out = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                r.lines().forEach(System.out::println);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // stderr
        Thread err = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                r.lines().forEach(System.err::println);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        out.start();
        err.start();

        int code = process.waitFor();
        out.join();
        err.join();
        return code;
    }

    private Map<String, String> merge(Map<String, String> values, Map<String, String> defaults) {
        HashMap<String, String> result = new HashMap<>();
        result.putAll(defaults);
        result.putAll(values);
        return result;
    }


}
