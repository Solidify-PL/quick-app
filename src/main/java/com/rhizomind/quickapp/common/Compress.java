package com.rhizomind.quickapp.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class Compress {

    public static File extractTarGz(File tarGzFile) throws IOException {
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
}
