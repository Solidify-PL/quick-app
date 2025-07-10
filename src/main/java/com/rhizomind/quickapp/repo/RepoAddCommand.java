package com.rhizomind.quickapp.repo;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

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
    private String repoUrl;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
