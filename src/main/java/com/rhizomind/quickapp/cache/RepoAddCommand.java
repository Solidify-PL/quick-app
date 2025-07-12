package com.rhizomind.quickapp.cache;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.net.URL;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "add",
        mixinStandardHelpOptions = true,
        description = "Adds QuickApp repository to local list of repositories"
)
public class RepoAddCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Nazwa repozytorium QuickApp.")
    private String repoName;

    @Parameters(index = "1", description = "URL repozytorium QuickApp.")
    private URL repoUrl;

    @Override
    public Integer call() throws Exception {
        System.out.println("Adding repository " + repoName + " to local list...");
        var repoList = Fixtures.getRepoList();
        var existingRepo = repoList.getRepositories()
                .stream()
                .filter(repo -> repo.getName().equals(repoName))
                .findAny();
        if (!existingRepo.isEmpty()) {
            throw new RuntimeException("Repository " + repoName + " already exists in local list.");
        }
        Repo repo = new Repo(repoName, repoUrl);
        repoList.getRepositories().add(repo);
        Fixtures.saveRepoList(repoList);


        System.out.println("Updating repositories index...");
        Fixtures.updateIndex(repo);

        return 0;
    }

}
