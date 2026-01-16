package com.rhizomind.quickapp.cache.command;

import com.rhizomind.quickapp.cache.Repo;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;

@CommandLine.Command(
        name = "list",
        mixinStandardHelpOptions = true,
        description = "Lists all local repositories"
)
public class RepoListCommand implements Callable<Integer> {

    @ParentCommand
    RepoCommand parent;

    @Override
    public Integer call() throws Exception {
        var repositories = parent.getConfig().getRepositories();

        printReposAsTable(repositories);
        return 0;
    }

    public static void printReposAsTable(List<Repo> repos) {
        if (repos == null || repos.isEmpty()) {
            System.out.println("No repositories to display.");
            return;
        }

        // Determine maximum length for column alignment
        int maxNameLength = "Name".length();
        int maxUrlLength = "URL".length();
        for (Repo repo : repos) {
            maxNameLength = Math.max(maxNameLength,
                    repo.getName() != null ? repo.getName().length() : 0);
            maxUrlLength = Math.max(maxUrlLength,
                    repo.getUrl() != null ? repo.getUrl().toString().length() : 0);
        }

        // Build format for headers and rows
        String format = "| %-" + maxNameLength + "s | %-" + maxUrlLength + "s |%n";
        String separator =
                "+" + "-".repeat(maxNameLength + 2) + "+" + "-".repeat(maxUrlLength + 2) + "+";

        // Print headers
        System.out.println(separator);
        System.out.printf(format, "Name", "URL");
        System.out.println(separator);

        // Print rows
        for (Repo repo : repos) {
            String name = repo.getName() != null ? repo.getName() : "";
            String url = repo.getUrl() != null ? repo.getUrl().toString() : "";
            System.out.printf(format, name, url);
        }

        // Print table bottom border
        System.out.println(separator);
    }
}
