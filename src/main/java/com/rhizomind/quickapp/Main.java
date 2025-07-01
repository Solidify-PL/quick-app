package com.rhizomind.quickapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        name = "checksum",
        mixinStandardHelpOptions = true,
        version = "checksum 4.0",
        description = "Prints the checksum (SHA-256 by default) of a file to STDOUT."
)
class Main implements Callable<Integer> {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
    @Option(names = {"-v", "--values"}, description = "Values yaml ")
    private File valuesFile;

    @Option(names = {"--value"},
            description = "Parametry nadpisujace wartosci z pliku values --value <nazwa>=<wartość>",
            paramLabel = "<nazwa>=<wartość>")
    private Map<String, String> values = new HashMap<>();

    @Option(names = {"-t", "--template"}, description = "template directory")
    private File templateDir;

    @Option(names = {"-o",
            "--output"}, description = "Output directory (Workdir by default) ", required = true)
    private File outputDir;

    @Option(names = {"-f", "--force"}, description = "Rewrite output", defaultValue = "false")
    private boolean forceRewrite;


    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

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

        var templateFilesDir = new File(templateDir, "files");
        var manifest = loadManifest(new FileInputStream(new File(templateDir, "manifest.yaml")));
        var values = loadMapParameters(this.valuesFile);
        var defaults = loadMapParameters(new File(templateDir, manifest.getValues().getDefaults()));
        var merge = merge(this.values, merge(values, defaults));

        var tempValuesFile = Files.createTempFile("quickapp", "values").toFile();

        OBJECT_MAPPER.writeValue(tempValuesFile, merge);
        System.out.println("Using following values:");
        OBJECT_MAPPER.writeValue(System.out, merge);
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

    public static void copyDirectoryWithAttributes(Path sourceDir, Path targetDir)
            throws IOException {
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("Source must exist and be a directory");
        }

        // Tworzymy katalog docelowy jeśli nie istnieje
        Files.createDirectories(targetDir);

        // Kopiujemy wszystkie pliki i katalogi
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Path relativePath = sourceDir.relativize(dir);
                Path targetPath = targetDir.resolve(relativePath);
                Files.createDirectories(targetPath);
                Files.setLastModifiedTime(targetPath, Files.getLastModifiedTime(dir));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Path relativePath = sourceDir.relativize(file);
                Path targetPath = targetDir.resolve(relativePath);
                Files.copy(file, targetPath, StandardCopyOption.COPY_ATTRIBUTES,
                        StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Map<String, Object> mergeMaps(Map<String, Object> base,
            Map<String, Object> overrides) {
        if (base == null) {
            base = new HashMap<>();
        }
        if (overrides == null) {
            return base;
        }

        Map<String, Object> merged = new LinkedHashMap<>(base);

        for (Map.Entry<String, Object> entry : overrides.entrySet()) {
            String key = entry.getKey();
            Object overrideValue = entry.getValue();

            if (merged.containsKey(key)) {
                Object baseValue = merged.get(key);

                if (baseValue instanceof Map && overrideValue instanceof Map) {
                    merged.put(key, mergeMaps(
                            (Map<String, Object>) baseValue,
                            (Map<String, Object>) overrideValue
                    ));
                } else {
                    merged.put(key, overrideValue); // override scalar or list
                }
            } else {
                merged.put(key, overrideValue); // new key
            }
        }

        return merged;
    }


    private Map<String, String> loadMapParameters(File valuesFile) throws IOException {
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


    private Manifest loadManifest(InputStream inputStream) throws IOException {
        return new ObjectMapper(new YAMLFactory()).readValue(inputStream, Manifest.class);
    }

}
