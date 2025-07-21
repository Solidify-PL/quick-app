package com.rhizomind.quickapp.cache;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rhizomind.quickapp.Manifest;
import com.rhizomind.quickapp.TarGzTemplate;
import com.rhizomind.quickapp.Template;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class RepoCache {

    private final File repoCacheDir;
    private final Repo repo;

    public RepoCache(File cacheDir, Repo repository) throws IOException {
        this.repoCacheDir = new File(cacheDir, repository.getName());
        this.repo = repository;

        this.repoCacheDir.mkdirs();
        getIndexFile().createNewFile();
    }

    public List<Manifest> readCachedIndex() throws IOException {
        var repoIndexFile = getIndexFile();
        if (!repoIndexFile.exists()) {
            cacheIndex(fetchIndex(repo));
        }

        return OBJECT_MAPPER.readValue(repoIndexFile, new TypeReference<List<Manifest>>() {
        });
    }

    public void updateIndex() throws IOException {
        cacheIndex(fetchIndex(repo));
    }

    public File getTemplatePackageFile(String templateName, String version)
            throws IOException {
        var index = readCachedIndex();
        var matchingTemplate = index.stream()
                .filter(m -> m.getName().equals(templateName) && (version == null || m.getVersion()
                        .equals(version)))
                .max(SemVer::compareSemVer)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Template " + templateName + ":" + version + " not found."));

        System.out.println(
                "Template " + matchingTemplate.getName() + ":" + matchingTemplate.getVersion()
                        + " found in repository "
                        + repo.getName() + ". Fetching package...");

        var templatePackageFile = new File(repoCacheDir,
                matchingTemplate.getName() + "-" + matchingTemplate.getVersion() + ".tar.gz");
        if (!templatePackageFile.exists()) {
            fetchTemplatePackage(repo, matchingTemplate);
        } else {
            System.out.println(
                    "Template package already cached: " + templatePackageFile.getAbsolutePath());
        }
        return templatePackageFile;
    }

    private void fetchTemplatePackage(Repo repo, Manifest template)
            throws IOException {
        URL repoUrl = repo.getUrl();
        String packageFileName = template.getName() + "-" + template.getVersion() + ".tar.gz";

        File templatePackageFile = new File(repoCacheDir, packageFileName);

        String path = repoUrl.getPath();
        // Jeśli ścieżka nie kończy się na '/', dodaj go
        if (!path.endsWith("/")) {
            path += "/";
        }
        // Połącz z index.yaml
        path += packageFileName;
        URL url = new URL(repoUrl.getProtocol(), repoUrl.getHost(), repoUrl.getPort(), path);

        System.out.println("Downloading template package: " + url + " into local cache: "
                + templatePackageFile.getAbsolutePath() + " ...");

        try (var input = url.openStream(); var output = new FileOutputStream(templatePackageFile)) {
            byte[] buffer = new byte[8192]; // 8 KB bufor
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }

    private List<Manifest> fetchIndex(Repo repo) throws IOException {
        // Pobierz ścieżkę z URL-a
        URL repoUrl = repo.getUrl();
        String path = repoUrl.getPath();
        // Jeśli ścieżka nie kończy się na '/', dodaj go
        if (!path.endsWith("/")) {
            path += "/";
        }
        // Połącz z index.yaml
        path += "index.yaml";

        // Utwórz nowy URL
        URL url = new URL(repoUrl.getProtocol(), repoUrl.getHost(), repoUrl.getPort(), path);
        var index = OBJECT_MAPPER.readValue(
                url.openStream(),
                new TypeReference<List<Manifest>>() {
                }
        );
        return index;
    }

    private void cacheIndex(List<Manifest> index) throws IOException {
        OBJECT_MAPPER.writeValue(getIndexFile(), index);
    }

    private File getIndexFile() {
        return new File(repoCacheDir, "index.yaml");
    }

    public TarGzTemplate getTemplate(String name, String version) throws IOException {
        return new TarGzTemplate(name, getTemplatePackageFile(name, version));
    }
}
