package com.rhizomind.quickapp;

import com.rhizomind.quickapp.build.IndexCommand;
import com.rhizomind.quickapp.build.PackageAllCommand;
import com.rhizomind.quickapp.build.PackageCommand;
import com.rhizomind.quickapp.cache.RepoCommand;
import com.rhizomind.quickapp.generate.GenerateCommand;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(
        name = "qa",
        subcommands = {GenerateCommand.class, PackageCommand.class, IndexCommand.class, RepoCommand.class,
            PackageAllCommand.class},
        description = "QuickApp CLI tool",
        mixinStandardHelpOptions = true
)
public class Main implements Runnable {

    public static void main(String[] args) {
        Path cacheDirectory1 = Directories.ensureCacheDirectoryExists();
        Path configDirectory = Directories.ensureConfigDirectoryExists();
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
