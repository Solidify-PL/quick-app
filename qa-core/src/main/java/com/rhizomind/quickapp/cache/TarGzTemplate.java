package com.rhizomind.quickapp.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rhizomind.quickapp.Commons;
import com.rhizomind.quickapp.Compress;
import com.rhizomind.quickapp.model.Manifest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class TarGzTemplate implements Template {

    private final String templateName;
    private final File templateTarGzPackage;

    public TarGzTemplate(String templateName, File templateTarGzPackage) {
        this.templateName = templateName;
        this.templateTarGzPackage = templateTarGzPackage;
    }

    public File extractTemplate() throws IOException {
        return Compress.extractTemplate(templateTarGzPackage);
    }

    @Override
    public Manifest getManifest() throws IOException {
        return Commons.OBJECT_MAPPER.readValue(
                getEntryBytes(templateName + "/manifest.yaml"),
                Manifest.class
        );
    }

    @Override
    public Map<String, String> getDefaults() throws IOException {
        Manifest manifest = getManifest();
        return Commons.OBJECT_MAPPER.readValue(
                getEntryBytes(templateName + "/" + manifest.getValues().getDefaults()),
                new TypeReference<Map<String, String>>() {
                }
        );
    }

    private byte[] getEntryBytes(String tarGzEntry) throws IOException {
        tarGzEntry = tarGzEntry.replaceAll("/\\./", "/");
        byte[] result = null;
        try (FileInputStream fis = new FileInputStream(templateTarGzPackage);
                GzipCompressorInputStream gzis = new GzipCompressorInputStream(fis);
                TarArchiveInputStream tarInput = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;
            while ((entry = tarInput.getNextTarEntry()) != null) {

                if (tarGzEntry.equals(entry.getName())) {
                    byte[] content = new byte[(int) entry.getSize()];
                    int offset = 0;
                    int bytesRead;
                    while (offset < content.length &&
                            (bytesRead = tarInput.read(content, offset, content.length - offset))
                                    != -1) {
                        offset += bytesRead;
                    }
                    result = content;
                    break;
                }
            }
        }
        return result;
    }


}
