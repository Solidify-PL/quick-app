package com.rhizomind.quickapp.cache;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class Config {

    private final File configFile;
    private final File cacheDirectory;

    public List<Repo> getRepositories() throws IOException {
        return getRepoList().getRepositories();
    }

    public RepoCache addRepo(Repo repo) throws IOException {
        var repoList = getRepoList();
        var existingRepo = repoList.getRepositories()
                .stream()
                .filter(r -> r.getName().equals(repo.getName()))
                .findAny();
        if (!existingRepo.isEmpty()) {
            throw new RuntimeException(
                    "Repository " + repo.getName() + " already exists in local list.");
        }
        repoList.getRepositories().add(repo);
        saveRepoList(repoList.getRepositories());

        System.out.println("Updating repositories index...");
        RepoCache repoCache = getRepoCache(repo);
        repoCache.updateIndex();
        return repoCache;
    }


    public RepoList getRepoList() throws IOException {
        var repoList = new RepoList();
        if (configFile.exists() && configFile.isFile() && configFile.length() > 0) {
            repoList = OBJECT_MAPPER.readValue(configFile, RepoList.class);
            if (repoList.getRepositories() == null) {
                repoList = new RepoList();
            }
        }
        return repoList;
    }

    public void removeRepo(String repoName) throws IOException {
        var remainingRepos = getRepositories()
                .stream()
                .filter(repo -> !repo.getName().equals(repoName))
                .collect(Collectors.toList());

        saveRepoList(remainingRepos);
    }

    private void saveRepoList(List<Repo> value) throws IOException {
        OBJECT_MAPPER.writeValue(configFile, new RepoList(value));
    }

    @SneakyThrows
    public RepoCache getRepoCache(Repo repo) {
        return new RepoCache(cacheDirectory, repo);
    }

    public Optional<RepoCache> getRepoCache(String repository) throws IOException {
        return getRepoList()
                .getRepositories()
                .stream()
                .filter(r -> r.getName().equals(repository))
                .findFirst()
                .map(this::getRepoCache);
    }
}
