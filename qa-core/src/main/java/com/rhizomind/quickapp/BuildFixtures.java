package com.rhizomind.quickapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.rhizomind.quickapp.model.Manifest;

import java.io.*;

public class BuildFixtures {

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

    public static void doPackage(File templateDir, File outputDir) throws IOException {
        Manifest manifest = Commons.loadManifest(new File(templateDir, "manifest.yaml"));
        File targetFile = new File(outputDir,
                manifest.getName() + "-" + manifest.getVersion() + ".tar.gz");
        System.out.println("Packaging template  into " + targetFile + " file...");

        Compress.createTarGz(templateDir, manifest.getName(), targetFile);
        System.out.println("Template packaged into " + targetFile + " file successfully.");
    }
}
