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
        description = "Search for templates in a local repository"
)
public class RepoSearchCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "QuickApp repository name.")
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
        // Column width determination
        int nameWidth = 20; // Minimum width for Name column
        int versionWidth = 10; // Minimum width for Version column
        int descriptionWidth = 50; // Minimum width for Description column

        // Calculate maximum length of fields to adjust column width
        for (Manifest manifest : manifests) {
            nameWidth = Math.max(nameWidth,
                    manifest.getName() != null ? manifest.getName().length() : 0);
            versionWidth = Math.max(versionWidth,
                    manifest.getVersion() != null ? manifest.getVersion().length() : 0);
            descriptionWidth = Math.max(descriptionWidth,
                    manifest.getDescription() != null ? manifest.getDescription().length() : 0);
        }

        // Build row format
        String format = "| %-" + nameWidth + "s | %-" + versionWidth + "s | %-" + descriptionWidth
                + "s |%n";
        String separator =
                "+" + "-".repeat(nameWidth + 2) + "+" + "-".repeat(versionWidth + 2) + "+"
                        + "-".repeat(descriptionWidth + 2) + "+";

        // Display header
        System.out.println(separator);
        System.out.printf(format, "Name", "Version", "Description");
        System.out.println(separator);

        // Display rows
        for (Manifest manifest : manifests) {
            String name = manifest.getName() != null ? manifest.getName() : "";
            String version = manifest.getVersion() != null ? manifest.getVersion() : "";
            String description = manifest.getDescription() != null ? manifest.getDescription() : "";
            System.out.printf(format, name, version, description);
        }

        // Display table bottom border
        System.out.println(separator);
    }
}
