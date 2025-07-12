package com.rhizomind.quickapp.cache;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rhizomind.quickapp.Directories;
import com.rhizomind.quickapp.Manifest;
import com.rhizomind.quickapp.generate.TemplateRef;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Fixtures {

    public static void saveRepoList(RepoList repoList) throws IOException {
        var repositoriesFile = new File(
                Directories.ensureConfigDirectoryExists().toFile(),
                "repositories.yaml"
        );

        OBJECT_MAPPER.writeValue(repositoriesFile, repoList);
    }

    public static RepoList getRepoList() throws IOException {
        var repositoriesFile = new File(
                Directories.ensureConfigDirectoryExists().toFile(),
                "repositories.yaml"
        );

        var repoList = new RepoList();
        if (repositoriesFile.exists()) {
            repoList = OBJECT_MAPPER.readValue(repositoriesFile, RepoList.class);
            if (repoList.getRepositories() == null) {
                repoList = new RepoList();
            }
        }
        return repoList;
    }


    public static List<Manifest> fetchIndex(URL repoUrl) throws IOException {
        if (repoUrl == null) {
            throw new IllegalArgumentException("baseUrl nie może być null.");
        }

        // Pobierz ścieżkę z URL-a
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

    private static void fetchTemplatePackage(Repo repo, Manifest template)
            throws IOException {
        URL repoUrl = repo.getUrl();
        String packageFileName = template.getName() + "-" + template.getVersion() + ".tar.gz";

        File repoDir = getRepoDir(repo);
        File templatePackageFile = new File(repoDir, packageFileName);

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


    public static List<Manifest> readCachedIndex(Repo repository) throws IOException {
        var repoIndexFile = getRepoIndexFile(repository);

        if (!repoIndexFile.exists()) {
            cacheIndex(
                    fetchIndex(repository.getUrl()),
                    repository.getName()
            );
        }

        return OBJECT_MAPPER.readValue(repoIndexFile, new TypeReference<List<Manifest>>() {
        });
    }

    public static void cacheIndex(List<Manifest> index, String repoName1) throws IOException {
        File repoDirectory = new File(Directories.ensureCacheDirectoryExists().toFile(), repoName1);
        repoDirectory.mkdirs();
        File indexFile = new File(repoDirectory, "index.yaml");
        OBJECT_MAPPER.writeValue(indexFile, index);
    }

    public static File getTemplatePackageFile(TemplateRef templateRef) throws IOException {
        String repoName = templateRef.getRepository();
        String templateName = templateRef.getName();
        String version = templateRef.getVersion();

        var matchingRepo = Fixtures.getRepoList()
                .getRepositories()
                .stream()
                .filter(r -> r.getName().equals(repoName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Repository " + repoName + " not found."));
        var index = readCachedIndex(matchingRepo);
        var matchingTemplate = index.stream()
                .filter(m -> m.getName().equals(templateName) && (version == null || m.getVersion()
                        .equals(version)))
                .max(Fixtures::compareSemVer)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Template " + templateName + ":" + version + " not found."));

        System.out.println(
                "Template " + matchingTemplate.getName() + ":" + matchingTemplate.getVersion()
                        + " found in repository "
                        + matchingRepo.getName() + ". Fetching package...");

        var matchingRepoDir = getRepoDir(matchingRepo);
        var templatePackageFile = new File(matchingRepoDir,
                matchingTemplate.getName() + "-" + matchingTemplate.getVersion() + ".tar.gz");
        if (!templatePackageFile.exists()) {
            fetchTemplatePackage(matchingRepo, matchingTemplate);
        } else {
            System.out.println(
                    "Template package already cached: " + templatePackageFile.getAbsolutePath());
        }
        return templatePackageFile;
    }

    public static int compareSemVer(Manifest v1, Manifest v2) {
        return compareSemVer(v1.getVersion(), v2.getVersion());
    }

    public static int compareSemVer(String v1, String v2) {
        int[] parts1 = parseSemVer(v1);
        int[] parts2 = parseSemVer(v2);

        for (int i = 0; i < 3; i++) {
            int cmp = Integer.compare(parts1[i], parts2[i]);
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0; // wersje są równe
    }

    private static int[] parseSemVer(String version) {
        String[] parts = version.split("\\.");
        int[] nums = new int[3];

        for (int i = 0; i < 3; i++) {
            if (i < parts.length) {
                String numPart = parts[i].replaceAll("[^0-9]", ""); // ignoruj np. "-alpha"
                nums[i] = numPart.isEmpty() ? 0 : Integer.parseInt(numPart);
            } else {
                nums[i] = 0;
            }
        }

        return nums;
    }

    private static File getRepoIndexFile(Repo repository) {
        var repoCacheDirectory = getRepoDir(repository);
        repoCacheDirectory.mkdirs();
        File repoIndexFile = new File(repoCacheDirectory, "index.yaml");
        return repoIndexFile;
    }

    private static File getRepoDir(Repo matchingRepo) {
        return new File(Directories.ensureCacheDirectoryExists().toFile(), matchingRepo.getName());
    }

    public static void updateIndex(Repo repo) throws IOException {
        var index = fetchIndex(repo.getUrl());
        cacheIndex(index, repo.getName());
    }
}
