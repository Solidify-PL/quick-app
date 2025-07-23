package com.rhizomind.quickapp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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

            TarArchiveEntry entry;
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

                    applyPermissions(outFile, entry.getMode());
                }
            }
        }
        return dstDir;
    }

    private static void applyPermissions(File file, int mode) {
        try {
            Set<PosixFilePermission> perms = new HashSet<>();

            if ((mode & 0400) != 0) perms.add(PosixFilePermission.OWNER_READ);
            if ((mode & 0200) != 0) perms.add(PosixFilePermission.OWNER_WRITE);
            if ((mode & 0100) != 0) perms.add(PosixFilePermission.OWNER_EXECUTE);

            if ((mode & 0040) != 0) perms.add(PosixFilePermission.GROUP_READ);
            if ((mode & 0020) != 0) perms.add(PosixFilePermission.GROUP_WRITE);
            if ((mode & 0010) != 0) perms.add(PosixFilePermission.GROUP_EXECUTE);

            if ((mode & 0004) != 0) perms.add(PosixFilePermission.OTHERS_READ);
            if ((mode & 0002) != 0) perms.add(PosixFilePermission.OTHERS_WRITE);
            if ((mode & 0001) != 0) perms.add(PosixFilePermission.OTHERS_EXECUTE);

            Files.setPosixFilePermissions(file.toPath(), perms);
        } catch (UnsupportedOperationException e) {
            // Windows - zignoruj
        } catch (IOException e) {
            System.err.println("Nie udało się ustawić chmod na " + file + ": " + e.getMessage());
        }
    }

    public static File compress(File sourceDir) throws IOException {
        if (!sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Podana ścieżka nie jest katalogiem: " + sourceDir);
        }

        File tempZip = Files.createTempFile("archive-", ".zip").toFile();
        tempZip.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempZip);
                ZipOutputStream zos = new ZipOutputStream(fos)) {

            Path basePath = sourceDir.toPath().toAbsolutePath();

            Files.walk(sourceDir.toPath())
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(
                                basePath.relativize(path).toString().replace("\\", "/"));
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
        return tempZip;
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
                    int mode = getFileModeCrossPlatform(file);
                    entry.setMode(mode);
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

    private static int getFileModeCrossPlatform(Path path) {
        try {
            PosixFileAttributes posix = Files.readAttributes(path, PosixFileAttributes.class);
            Set<PosixFilePermission> permissions = posix.permissions();
            return permissionsToMode(permissions);
        } catch (UnsupportedOperationException e) {
            // Windows: brak POSIX
            String name = path.getFileName().toString().toLowerCase();
            if (name.endsWith(".sh") || name.endsWith(".exe") || name.endsWith(".bat")) {
                return 0755; // executable
            }
            return 0644; // standardowy plik
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int permissionsToMode(Set<PosixFilePermission> permissions) {
        int mode = 0;
        if (permissions.contains(PosixFilePermission.OWNER_READ)) mode |= 0400;
        if (permissions.contains(PosixFilePermission.OWNER_WRITE)) mode |= 0200;
        if (permissions.contains(PosixFilePermission.OWNER_EXECUTE)) mode |= 0100;

        if (permissions.contains(PosixFilePermission.GROUP_READ)) mode |= 0040;
        if (permissions.contains(PosixFilePermission.GROUP_WRITE)) mode |= 0020;
        if (permissions.contains(PosixFilePermission.GROUP_EXECUTE)) mode |= 0010;

        if (permissions.contains(PosixFilePermission.OTHERS_READ)) mode |= 0004;
        if (permissions.contains(PosixFilePermission.OTHERS_WRITE)) mode |= 0002;
        if (permissions.contains(PosixFilePermission.OTHERS_EXECUTE)) mode |= 0001;

        return mode;
    }
}
