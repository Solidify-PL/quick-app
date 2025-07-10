package com.rhizomind.quickapp.repo;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "remove",
        mixinStandardHelpOptions = true,
        description = "Remove local repository"
)
public class RepoRemoveCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Nazwa repozytorium QuickApp.")
    private String repoName;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
