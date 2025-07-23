package com.rhizomind.quickapp.cache.command;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@CommandLine.Command(
        name = "remove",
        mixinStandardHelpOptions = true,
        description = "Remove local repository"
)
public class RepoRemoveCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Nazwa repozytorium QuickApp.")
    private String repoName;

    @ParentCommand
    RepoCommand parent;


    @Override
    public Integer call() throws Exception {

        parent.getConfig().removeRepo(repoName);

        return 0;
    }
}
