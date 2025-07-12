package com.rhizomind.quickapp.cache;

import picocli.CommandLine;

@CommandLine.Command(
        name = "repo",
        subcommands = {RepoAddCommand.class, RepoListCommand.class, RepoRemoveCommand.class, RepoSearchCommand.class, RepoUpdateCommand.class},
        description = "Add, List, Remove, Search local repositories",
        mixinStandardHelpOptions = true
)
public class RepoCommand implements Runnable{

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        new CommandLine(spec).usage(System.out);
    }
}
