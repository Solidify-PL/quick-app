package com.rhizomind.quickapp;

import com.rhizomind.quickapp.model.Manifest;
import com.rhizomind.quickapp.model.ManifestLoadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestLoaderTest {

    @Test
    void parsesValidManifest() throws Exception {
        String yaml = """
                name: my-template
                version: 1.2.3
                description: A valid manifest
                tags:
                  - java
                  - cli
                """;

        Manifest manifest = Commons.loadManifest(inputStream(yaml), "inline");

        assertEquals("my-template", manifest.getName());
        assertEquals("1.2.3", manifest.getVersion());
        assertEquals("A valid manifest", manifest.getDescription());
        assertEquals(2, manifest.getTags().size());
    }

    @Test
    void invalidYamlReportsLineAndSource() {
        // Unterminated double-quoted string — YAML parser error
        String broken = "name: \"my-template\nversion: 1.0.0\n";

        ManifestLoadException ex = assertThrows(
                ManifestLoadException.class,
                () -> Commons.loadManifest(inputStream(broken), "manifest.yaml")
        );

        assertEquals(ManifestLoadException.Kind.INVALID_YAML, ex.getKind());
        assertEquals("manifest.yaml", ex.getSource());
        assertTrue(ex.getLine() > 0, "line number should be reported, was: " + ex.getLine());
        assertTrue(ex.getMessage().contains("manifest.yaml"),
                "message should include source, was: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("line"),
                "message should mention line, was: " + ex.getMessage());
    }

    @Test
    void schemaMismatchOnScalarFieldReportsFieldPath() {
        // `name` is declared as String but a list is provided
        String yaml = """
                name:
                  - not
                  - a
                  - string
                version: 1.0.0
                """;

        ManifestLoadException ex = assertThrows(
                ManifestLoadException.class,
                () -> Commons.loadManifest(inputStream(yaml), "manifest.yaml")
        );

        assertEquals(ManifestLoadException.Kind.INVALID_SCHEMA, ex.getKind());
        assertEquals("manifest.yaml", ex.getSource());
        assertNotNull(ex.getFieldPath());
        assertTrue(ex.getFieldPath().contains("name"),
                "field path should contain 'name', was: " + ex.getFieldPath());
        assertTrue(ex.getLine() > 0, "line should be reported, was: " + ex.getLine());
        assertTrue(ex.getMessage().contains("name"),
                "message should reference the field, was: " + ex.getMessage());
    }

    @Test
    void schemaMismatchOnNestedObjectReportsFullPath() {
        // `values` is declared as ValuesConfig (object) but a scalar is provided
        String yaml = """
                name: my-template
                version: 1.0.0
                values: just-a-string
                """;

        ManifestLoadException ex = assertThrows(
                ManifestLoadException.class,
                () -> Commons.loadManifest(inputStream(yaml), "manifest.yaml")
        );

        assertEquals(ManifestLoadException.Kind.INVALID_SCHEMA, ex.getKind());
        assertNotNull(ex.getFieldPath());
        assertTrue(ex.getFieldPath().contains("values"),
                "field path should contain 'values', was: " + ex.getFieldPath());
    }

    @Test
    void loadManifestFromFileUsesFilePathAsSource(@TempDir Path tempDir) throws Exception {
        Path manifestFile = tempDir.resolve("manifest.yaml");
        Files.writeString(manifestFile, """
                name: my-template
                version: [1, 2, 3]
                """);

        ManifestLoadException ex = assertThrows(
                ManifestLoadException.class,
                () -> Commons.loadManifest(manifestFile.toFile())
        );

        assertEquals(ManifestLoadException.Kind.INVALID_SCHEMA, ex.getKind());
        assertEquals(manifestFile.toFile().getAbsolutePath(), ex.getSource());
        assertTrue(ex.getMessage().contains(manifestFile.toFile().getAbsolutePath()),
                "message should contain the absolute file path, was: " + ex.getMessage());
    }

    @Test
    void loadManifestFromBytesCarriesSource() {
        String yaml = "this is not: valid: yaml: at all:";
        ManifestLoadException ex = assertThrows(
                ManifestLoadException.class,
                () -> Commons.loadManifest(yaml.getBytes(StandardCharsets.UTF_8), "archive.tar.gz!template/manifest.yaml")
        );

        assertEquals("archive.tar.gz!template/manifest.yaml", ex.getSource());
        assertTrue(ex.getMessage().contains("archive.tar.gz!template/manifest.yaml"));
    }

    @Test
    void nonExistentFileStillProducesIoException(@TempDir Path tempDir) {
        File missing = tempDir.resolve("does-not-exist.yaml").toFile();
        // Regular IOException (file-not-found) is not translated to ManifestLoadException —
        // ManifestLoadException is reserved for YAML/schema problems in content that we *did* read.
        assertThrows(java.io.IOException.class, () -> Commons.loadManifest(missing));
    }

    private static ByteArrayInputStream inputStream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
