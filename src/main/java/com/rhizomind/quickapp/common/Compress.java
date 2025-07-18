package com.rhizomind.quickapp.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class Compress {

    public static File extractTemplate(File tarGzFile) throws IOException {
        var dstDir = Files.createTempDirectory(tarGzFile.getName()).toFile();

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
        return dstDir;
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
}
