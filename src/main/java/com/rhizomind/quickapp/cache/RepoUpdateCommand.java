package com.rhizomind.quickapp.cache;

import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "update",
        mixinStandardHelpOptions = true,
        description = "Updates cached index"
)
public class RepoUpdateCommand implements Callable<Integer> {

    @Option(names = {"-r", "--repo"}, description = "Name of repo")
    private String repoName;


    @Override
    public Integer call() throws Exception {
        var reposToUpdate = Fixtures.getRepoList().getRepositories();
        if (repoName != null) {
            reposToUpdate = List.of(reposToUpdate
                    .stream()
                    .filter(repo -> repo.getName().equals(repoName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unknown repo: " + repoName))
            );
        }

        reposToUpdate.forEach(repo -> {
            System.out.println(
                    "Updating index for repo " + repo.getName() + " (" + repo.getUrl() + ")...");
            try {
                Fixtures.updateIndex(repo);
            } catch (Exception e) {
                System.err.println(
                        "Error updating index for repo " + repo.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        return 0;
    }

}
