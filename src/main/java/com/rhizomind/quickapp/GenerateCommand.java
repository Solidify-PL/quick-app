package com.rhizomind.quickapp;

import com.rhizomind.quickapp.repo.Fixtures;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
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
class GenerateCommand implements Callable<Integer> {

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
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                throw new IllegalArgumentException(
                        "Output directory '" + outputDir.getAbsolutePath()
                                + "' is not a directory");
            }
        } else {
            outputDir.mkdirs();
        }

        File templateFilesDir = null;

        var classify = classify(templateDirOrName);
        if (classify == TemplateInputType.TEMPLATE_REF) {
            String[] strings = parseTemplateRef(templateDirOrName);
            templateFilesDir = Files.createTempDirectory("quickapp").toFile();
            extractTarGz(
                    Fixtures.getTemplatePackageFile(
                            strings[0],
                            strings[1],
                            strings.length > 2 ? strings[2] : null
                    ),
                    templateFilesDir
            );
            templateFilesDir = new File(templateFilesDir, strings[1]);
        } else {
            templateFilesDir = new File(templateDirOrName, "files");
        }

        var manifest = Commons.loadManifest(
                new FileInputStream(new File(templateFilesDir, "manifest.yaml")));
        var values = Commons.loadMapParameters(this.valuesFile);
        var defaults = Commons.loadMapParameters(
                new File(templateFilesDir, manifest.getValues().getDefaults()));
        var merge = merge(this.values, merge(values, defaults));

        var tempValuesFile = Files.createTempFile("quickapp", "values").toFile();

        Commons.OBJECT_MAPPER.writeValue(tempValuesFile, merge);
        System.out.println("Using following values:");
        System.out.println(Commons.OBJECT_MAPPER.writeValueAsString(merge));

        var command = "docker run --rm -u $(id -u):$(id -g) "
                + " -v " + new File(templateFilesDir, "files").getAbsolutePath()
                + ":/tmp/quickapp/input:ro "
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
        System.out.flush();
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

    private static final Pattern TEMPLATE_REF_PATTERN =
            Pattern.compile("^[^/\\\\:]+/[^/\\\\:]+(:[^/\\\\:]+)?$");

    public enum TemplateInputType {
        TEMPLATE_REF, // <repo>/<template>[:version]
        LOCAL_PATH
    }

    public static TemplateInputType classify(String templateDirOrName) {
        if (TEMPLATE_REF_PATTERN.matcher(templateDirOrName).matches()) {
            return TemplateInputType.TEMPLATE_REF;
        } else {
            return TemplateInputType.LOCAL_PATH;
        }
    }

    public static void extractTarGz(File tarGzFile, File dstDir) throws IOException {
        if (!dstDir.exists()) {
            if (!dstDir.mkdirs()) {
                throw new IOException("Could not create destination directory: " + dstDir);
            }
        }

        try (InputStream fi = new FileInputStream(tarGzFile);
                InputStream gzi = new GzipCompressorInputStream(fi);
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzi)) {

            ArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                File outFile = new File(dstDir, entry.getName());

                if (entry.isDirectory()) {
                    if (!outFile.exists() && !outFile.mkdirs()) {
                        throw new IOException("Could not create directory: " + outFile);
                    }
                } else {
                    File parent = outFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Could not create parent directory: " + parent);
                    }
                    try (OutputStream out = Files.newOutputStream(outFile.toPath())) {
                        tarIn.transferTo(out);
                    }
                }
            }
        }

    }

    public static String[] parseTemplateRef(String input) {
        if (input == null || !input.contains("/")) {
            throw new IllegalArgumentException(
                    "Invalid format: expected <repo>/<template>[:version]");
        }

        String[] repoAndRest = input.split("/", 2);
        String repo = repoAndRest[0];
        String templateAndMaybeVersion = repoAndRest[1];

        String[] templateAndVersion = templateAndMaybeVersion.split(":", 2);
        if (templateAndVersion.length == 2) {
            return new String[]{repo, templateAndVersion[0], templateAndVersion[1]};
        } else {
            return new String[]{repo, templateAndVersion[0]};
        }
    }
}
