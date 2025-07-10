package com.rhizomind.quickapp;

import com.rhizomind.quickapp.repo.RepoCommand;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(
        name = "qa",
        subcommands = {GenerateCommand.class, PackageCommand.class, IndexCommand.class, RepoCommand.class},
        description = "QuickApp CLI tool",
        mixinStandardHelpOptions = true
)
public class Main implements Runnable {

    public static void main(String[] args) {
        Path cacheDirectory1 = Directories.createCacheDirectory();
        Path configDirectory = Directories.createConfigDirectory();
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        new CommandLine(spec).usage(System.out);
    }
}
