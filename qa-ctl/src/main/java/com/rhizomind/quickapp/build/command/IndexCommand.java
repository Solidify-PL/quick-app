package com.rhizomind.quickapp.build.command;

import com.rhizomind.quickapp.model.Manifest;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;

@CommandLine.Command(
        name = "index",
        mixinStandardHelpOptions = true,
        description = "Generate an index file (index.yaml) from template packages in a specified directory"
)
public class IndexCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-i",
            "--input"}, description = "Directory with templates packages (Workdir by default)")
    private File inputDir;

    @CommandLine.Option(names = {"-o",
            "--output"}, description = "Output directory (input by default) ")
    private File outputDir;

    @CommandLine.ParentCommand
    TemplatesCommand parent;

    @Override
    public Integer call() throws Exception {
        var inputDir = this.inputDir == null ? new File(".") : this.inputDir;
        var outputDir = this.outputDir == null ? inputDir : this.outputDir;

        var manifests = getTemplatePackage(inputDir)
                .stream()
                .map(file -> {
                    try {
                        System.out.print(
                                "Processing template package: " + file.getAbsolutePath() + " ... ");
                        var manifestO = Optional.of(getManifest(
                                file,
                                extractBaseName(file.getName())
                        ));
                        System.out.println("done.");
                        return manifestO;
                    } catch (IOException e) {
                        System.err.println("error: " + e.getMessage());
                        return Optional.<Manifest>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(Manifest::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Manifest::getVersion, Comparator.reverseOrder()))
                .collect(Collectors.toList());
        OBJECT_MAPPER.writeValue(new File(outputDir, "index.yaml"), manifests);
        return 0;
    }

    public static Collection<File> getTemplatePackage(File directory) {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException("The specified file must be a directory.");
        }

        File[] files = directory.listFiles((dir, name) -> isValidTarGzFileName(name));

        if (files == null) {
            throw new RuntimeException(
                    "Failed to read the contents of the directory: " + directory.getAbsolutePath());
        }
        return List.of(files);
    }

    public static Manifest getManifest(File tarGzFile, String templateName) throws IOException {
        if (!tarGzFile.exists() || !tarGzFile.isFile()) {
            throw new IllegalArgumentException(
                    "The specified file does not exist: " + tarGzFile.getAbsolutePath());
        }
        if (!isValidTarGzFileName(tarGzFile.getName())) {
            throw new IllegalArgumentException("The specified file is not a valid tar.gz file: "
                    + tarGzFile.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(tarGzFile);
                GzipCompressorInputStream gzis = new GzipCompressorInputStream(fis);
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;
            // Przeglądanie wpisów w archiwum
            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (!entry.isDirectory() && entry.getName()
                        .equals(templateName + "/manifest.yaml")) {
                    // Odczytanie zawartości pliku
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = tarIn.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }

                    // Parsowanie zawartości jako YAML
                    String yamlContent = baos.toString(StandardCharsets.UTF_8);
                    try {
                        return OBJECT_MAPPER.readValue(yamlContent, Manifest.class);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Invalid manifest file: " + tarGzFile.getAbsolutePath(), e);
                    }
                }
            }
        }
        throw new RuntimeException(
                "Manifest file not found in tar.gz file: " + tarGzFile.getAbsolutePath());
    }

    static String regex = "^([a-zA-Z0-9_-]+)-\\d+\\.\\d+(\\.\\d+)?\\.tar\\.gz$";
    public static final Pattern PATTERN = Pattern.compile(regex);

    public static boolean isValidTarGzFileName(String fileName) {
        // Regular expression:
        // ^([a-zA-Z0-9_-]+): Group 1 - any alphanumeric sequence with _ or -
        // - : Separating hyphen
        // \d+\.\d+(\.\d+)? : Version in X.Y or X.Y.Z format
        // \.tar\.gz$ : .tar.gz extension
        return PATTERN.matcher(fileName).matches();
    }

    public static String extractBaseName(String fileName) {
        Matcher matcher = PATTERN.matcher(fileName);

        if (matcher.matches()) {
            return matcher.group(1); // Returns group 1, i.e., 'any_name'
        }
        return null;
    }
}
