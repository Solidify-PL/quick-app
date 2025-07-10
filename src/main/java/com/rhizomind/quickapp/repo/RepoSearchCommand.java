package com.rhizomind.quickapp.repo;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "search",
        mixinStandardHelpOptions = true,
        description = "Search for template is local repository"
)
public class RepoSearchCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Nazwa repozytorium QuickApp.")
    private String repoName;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
