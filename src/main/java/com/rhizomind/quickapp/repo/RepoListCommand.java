package com.rhizomind.quickapp.repo;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "list",
        mixinStandardHelpOptions = true,
        description = "Lists all local repositories"
)
public class RepoListCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
