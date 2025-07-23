package com.rhizomind.quickapp.cache.command;

import com.rhizomind.quickapp.Main;
import com.rhizomind.quickapp.cache.Config;
import java.io.IOException;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@CommandLine.Command(
        name = "repo",
        subcommands = {RepoAddCommand.class, RepoListCommand.class, RepoRemoveCommand.class, RepoSearchCommand.class, RepoUpdateCommand.class},
        description = "Add, List, Remove, Search local repositories",
        mixinStandardHelpOptions = true
)
public class RepoCommand implements Runnable{

    @Spec
    CommandSpec spec;

    @ParentCommand
    Main parent;

    public Config getConfig() throws IOException {
        return parent.getConfig();
    }

    @Override
    public void run() {
        new CommandLine(spec).usage(System.out);
    }
}
