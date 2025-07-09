package com.rhizomind.quickapp;

import picocli.CommandLine;

@CommandLine.Command(
        name = "qa",
        subcommands = {GenerateCommand.class, PackageCommand.class, IndexCommand.class},
        description = "QuickApp CLI tool"
)
public class Main implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println(
                "Użyj jednej z komend: generate, package. Wpisz 'qa --help', aby zobaczyć szczegóły.");
    }
}
