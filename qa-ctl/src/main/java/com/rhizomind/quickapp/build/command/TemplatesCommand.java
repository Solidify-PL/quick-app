package com.rhizomind.quickapp.build.command;

import com.rhizomind.quickapp.Main;
import com.rhizomind.quickapp.cache.Config;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import java.io.IOException;

@CommandLine.Command(
        name = "templates",
        subcommands = {IndexCommand.class, PackageCommand.class, PackageAllCommand.class, TestCommand.class},
        description = "package, test, index templates",
        mixinStandardHelpOptions = true
)
public class TemplatesCommand implements Runnable {

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
