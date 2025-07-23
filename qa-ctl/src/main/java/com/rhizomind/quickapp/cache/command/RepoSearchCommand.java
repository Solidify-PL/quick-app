package com.rhizomind.quickapp.cache.command;

import com.rhizomind.quickapp.model.Manifest;
import com.rhizomind.quickapp.cache.Config;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@CommandLine.Command(
        name = "search",
        mixinStandardHelpOptions = true,
        description = "Search for template is local repository"
)
public class RepoSearchCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Nazwa repozytorium QuickApp.")
    private String repoName;

    @ParentCommand
    RepoCommand parent;


    @Override
    public Integer call() throws Exception {
        Config config = parent.getConfig();

        var repository = config.getRepoList().getRepositories()
                .stream()
                .filter(repo -> repo.getName().equals(repoName))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Repository " + repoName + " not found."));

        printManifestTable(config.getRepoCache(repository).readCachedIndex());

        return 0;
    }

    private void printManifestTable(List<Manifest> manifests) {
        // Określenie szerokości kolumn
        int nameWidth = 20; // Minimalna szerokość dla kolumny Name
        int versionWidth = 10; // Minimalna szerokość dla kolumny Version
        int descriptionWidth = 50; // Minimalna szerokość dla kolumny Description

        // Obliczenie maksymalnej długości pól, aby dostosować szerokość kolumn
        for (Manifest manifest : manifests) {
            nameWidth = Math.max(nameWidth,
                    manifest.getName() != null ? manifest.getName().length() : 0);
            versionWidth = Math.max(versionWidth,
                    manifest.getVersion() != null ? manifest.getVersion().length() : 0);
            descriptionWidth = Math.max(descriptionWidth,
                    manifest.getDescription() != null ? manifest.getDescription().length() : 0);
        }

        // Budowanie formatu dla wierszy
        String format = "| %-" + nameWidth + "s | %-" + versionWidth + "s | %-" + descriptionWidth
                + "s |%n";
        String separator =
                "+" + "-".repeat(nameWidth + 2) + "+" + "-".repeat(versionWidth + 2) + "+"
                        + "-".repeat(descriptionWidth + 2) + "+";

        // Wyświetlenie nagłówka
        System.out.println(separator);
        System.out.printf(format, "Name", "Version", "Description");
        System.out.println(separator);

        // Wyświetlenie wierszy
        for (Manifest manifest : manifests) {
            String name = manifest.getName() != null ? manifest.getName() : "";
            String version = manifest.getVersion() != null ? manifest.getVersion() : "";
            String description = manifest.getDescription() != null ? manifest.getDescription() : "";
            System.out.printf(format, name, version, description);
        }

        // Wyświetlenie dolnej krawędzi tabeli
        System.out.println(separator);
    }
}
