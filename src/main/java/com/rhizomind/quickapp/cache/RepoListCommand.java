package com.rhizomind.quickapp.cache;

import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "list",
        mixinStandardHelpOptions = true,
        description = "Lists all local repositories"
)
public class RepoListCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        printReposAsTable(Fixtures.getRepoList().getRepositories());

        return 0;
    }

    public static void printReposAsTable(List<Repo> repos) {
        if (repos == null || repos.isEmpty()) {
            System.out.println("Brak repozytoriów do wyświetlenia.");
            return;
        }

        // Określenie maksymalnej długości dla wyrównania kolumn
        int maxNameLength = "Name".length();
        int maxUrlLength = "URL".length();
        for (Repo repo : repos) {
            maxNameLength = Math.max(maxNameLength, repo.getName() != null ? repo.getName().length() : 0);
            maxUrlLength = Math.max(maxUrlLength, repo.getUrl() != null ? repo.getUrl().toString().length() : 0);
        }

        // Budowanie formatu dla nagłówków i wierszy
        String format = "| %-" + maxNameLength + "s | %-" + maxUrlLength + "s |%n";
        String separator = "+" + "-".repeat(maxNameLength + 2) + "+" + "-".repeat(maxUrlLength + 2) + "+";

        // Drukowanie nagłówków
        System.out.println(separator);
        System.out.printf(format, "Name", "URL");
        System.out.println(separator);

        // Drukowanie wierszy
        for (Repo repo : repos) {
            String name = repo.getName() != null ? repo.getName() : "";
            String url = repo.getUrl() != null ? repo.getUrl().toString() : "";
            System.out.printf(format, name, url);
        }

        // Drukowanie dolnej krawędzi tabeli
        System.out.println(separator);
    }
}
