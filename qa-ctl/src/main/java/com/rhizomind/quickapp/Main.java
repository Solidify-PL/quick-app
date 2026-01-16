package com.rhizomind.quickapp;

import static com.rhizomind.quickapp.Directories.defaultConfigFile;
import static com.rhizomind.quickapp.Directories.ensureCacheDirectoryExists;
import static com.rhizomind.quickapp.Directories.ensureConfigFileExists;
import static com.rhizomind.quickapp.Directories.getDefaultCacheDir;
import static picocli.CommandLine.ScopeType.INHERIT;

import com.rhizomind.quickapp.generate.command.DescribeCommand;
import com.rhizomind.quickapp.build.command.IndexCommand;
import com.rhizomind.quickapp.build.command.PackageAllCommand;
import com.rhizomind.quickapp.build.command.PackageCommand;
import com.rhizomind.quickapp.generate.command.TestCommand;
import com.rhizomind.quickapp.cache.Config;
import com.rhizomind.quickapp.cache.command.RepoCommand;
import com.rhizomind.quickapp.generate.command.GenerateCommand;
import java.io.File;
import java.io.IOException;
import picocli.CommandLine;
import picocli.CommandLine.Option;

@CommandLine.Command(
        name = "qa",
        subcommands = {
                GenerateCommand.class,
                PackageCommand.class,
                IndexCommand.class,
                RepoCommand.class,
                PackageAllCommand.class,
                TestCommand.class,
                DescribeCommand.class
        },
        description = "QuickApp CLI tool",
        mixinStandardHelpOptions = true
)
public class Main implements Runnable {

    @Option(names = "--config-file", description = "Path to the configuration file", scope = INHERIT, defaultValue = "${HOME}/.config/quick-app/repositories.yaml")
    private String configPath;
    @Option(names = "--cache-dir", description = "Path to the cache directory", scope = INHERIT, defaultValue = "${HOME}/.cache/quick-app")
    private String cacheDir;

    private Config config;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        new CommandLine(spec).usage(System.out);
    }

    public Config getConfig() throws IOException {
        if (config == null) {
            config = new Config(
                    ensureConfigFileExists(
                            configPath != null ? new File(configPath) : defaultConfigFile()
                    ),
                    ensureCacheDirectoryExists(
                            cacheDir != null ? new File(cacheDir) : getDefaultCacheDir()
                    )
            );
        }
        return config;
    }
}
