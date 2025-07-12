package com.rhizomind.quickapp.cache;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

@CommandLine.Command(
        name = "remove",
        mixinStandardHelpOptions = true,
        description = "Remove local repository"
)
public class RepoRemoveCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Nazwa repozytorium QuickApp.")
    private String repoName;

    @Override
    public Integer call() throws Exception {
        var repoList = Fixtures.getRepoList();

        var remainingRepos = repoList.getRepositories()
                .stream()
                .filter(repo -> !repo.getName().equals(repoName))
                .collect(Collectors.toList());

        repoList.setRepositories(remainingRepos);

        Fixtures.saveRepoList(repoList);
        return 0;
    }
}
