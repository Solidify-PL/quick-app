package com.rhizomind.quickapp.build;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.rhizomind.quickapp.Commons;
import com.rhizomind.quickapp.Manifest;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class BulidFixtures {

    public static boolean isValidTemplateDir(File templateDir) {
        return templateDir.exists()
                && templateDir.isDirectory()
                && new File(templateDir, "manifest.yaml").exists()
                && new File(templateDir, "parameters-defaults.yaml").exists()
                && new File(templateDir, "parameters-schema.json").exists()
                && new File(templateDir, "files").exists()
                && new File(templateDir, "files").isDirectory()
                && validateManifestFile(new File(templateDir, "manifest.yaml"))
                && validateSchemaFile(new File(templateDir, "parameters-schema.json"))
                && validateDefaultsFile(
                new File(templateDir, "parameters-schema.json"),
                new File(templateDir, "parameters-defaults.yaml"));
    }

    public static boolean validateDefaultsFile(File schemaFile, File defaultsFile) {
        try {
            JsonNode valuesNode = Commons.OBJECT_MAPPER.readTree(defaultsFile);
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(
                    SpecVersion.VersionFlag.V7);
            JsonSchema schema = schemaFactory.getSchema(new FileInputStream(schemaFile));

            var validationMessages = schema.validate(valuesNode);
            if (!validationMessages.isEmpty()) {
                System.out.println(
                        defaultsFile + " is not compatible with schema " + validationMessages);
            }
            return validationMessages.isEmpty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean validateSchemaFile(File schemaFile) {
        try {
            JsonNode schemaNode = Commons.OBJECT_MAPPER.readTree(schemaFile);
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(
                    SpecVersion.VersionFlag.V7);
            JsonSchema metaSchema = schemaFactory.getSchema(
                    JsonSchemaFactory.class.getResourceAsStream("/draft-07/schema"));
            var validationMessages = metaSchema.validate(schemaNode);
            if (!validationMessages.isEmpty()) {
                System.out.println(
                        schemaFile + " is a proper JsonSchema file " + validationMessages);
            }
            return validationMessages.isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(
                    schemaFile.getAbsolutePath() + " is not correct JSONSchema file", e);
        }
    }

    public static boolean validateManifestFile(File manifestFile) {
        try {
            Commons.loadManifest(manifestFile);
        } catch (IOException e) {
            System.out.println(manifestFile + " is not a valid manifest file " + e.getMessage());
            return false;
        }
        return true;
    }

    public static void createTarGz(File templateDir, String templateName, File dstFile)
            throws IOException {
        // Tworzenie pliku tar.gz
        try (FileOutputStream fos = new FileOutputStream(dstFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
                TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzos)) {
            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

            var templateDirPath = templateDir.toPath();
            Files.walkFileTree(templateDirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    // Tworzenie ścieżki względnej względem sourceDir
                    Path relativePath = templateDirPath.relativize(file);
                    // Dodanie templateName jako prefiksu w archiwum
                    String entryName =
                            templateName + "/" + relativePath.toString().replace("\\", "/");

                    // Tworzenie wpisu TAR
                    TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), entryName);
                    tarOut.putArchiveEntry(entry);

                    // Zapisanie zawartości pliku do archiwum
                    Files.copy(file, tarOut);
                    tarOut.closeArchiveEntry();

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    // Tworzenie ścieżki względnej i dodanie templateName
                    Path relativePath = templateDirPath.relativize(dir);
                    String entryName = relativePath.toString().isEmpty()
                            ? templateName + "/"
                            : templateName + "/" + relativePath.toString().replace("\\", "/") + "/";

                    // Tworzenie wpisu dla katalogu
                    TarArchiveEntry entry = new TarArchiveEntry(entryName);
                    tarOut.putArchiveEntry(entry);
                    tarOut.closeArchiveEntry();

                    return FileVisitResult.CONTINUE;
                }
            });
            tarOut.finish();
        }
    }

    public static void doPackage(File templateDir, File outputDir) throws IOException {
        Manifest manifest = Commons.loadManifest(new File(templateDir, "manifest.yaml"));
        File targetFile = new File(outputDir,
                manifest.getName() + "-" + manifest.getVersion() + ".tar.gz");
        System.out.println("Packaging template  into " + targetFile + " file...");

        createTarGz(templateDir, manifest.getName(), targetFile);
        System.out.println("Template packaged into " + targetFile + " file successfully.");
    }
}
