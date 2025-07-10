package com.rhizomind.quickapp.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rhizomind.quickapp.Directories;
import com.rhizomind.quickapp.Manifest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;

public class Fixtures {

    public static void saveRepoList(RepoList repoList) throws IOException {
        var repositoriesFile = new File(Directories.createConfigDirectory().toFile(),
                "repositories.yaml");

        OBJECT_MAPPER.writeValue(repositoriesFile, repoList);
    }

    public static RepoList getRepoList() throws IOException {
        var repositoriesFile = new File(Directories.createConfigDirectory().toFile(),
                "repositories.yaml");

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

    public static List<Manifest> readIndex(Repo repository) throws IOException {
        File repoCacheDirectory = new File(Directories.createCacheDirectory().toFile(),
                repository.getName());
        repoCacheDirectory.mkdirs();
        File repoIndexFile = new File(repoCacheDirectory, "index.yaml");

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
        File repoDirectory = new File(Directories.createCacheDirectory().toFile(), repoName1);
        repoDirectory.mkdirs();
        File indexFile = new File(repoDirectory, "index.yaml");
        OBJECT_MAPPER.writeValue(indexFile, index);
    }

}
