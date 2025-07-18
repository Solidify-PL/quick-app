package com.rhizomind.quickapp.cache.command;

import com.rhizomind.quickapp.cache.Repo;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.net.URL;
import java.util.concurrent.Callable;
import picocli.CommandLine.ParentCommand;

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

    @ParentCommand
    RepoCommand parent;

    @Override
    public Integer call() throws Exception {
        System.out.println("Adding repository " + repoName + " to local list...");
        parent.getConfig().addRepo(new Repo(repoName, repoUrl));

        return 0;
    }

}
