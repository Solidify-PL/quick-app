package com.rhizomind.quickapp.generate.command;

import static com.rhizomind.quickapp.common.Compress.extractTemplate;
import static com.rhizomind.quickapp.common.Process.execute;
import static com.rhizomind.quickapp.generate.TemplateRef.isTemplateRef;
import static com.rhizomind.quickapp.generate.TemplateRef.parse;

import com.rhizomind.quickapp.Main;
import com.rhizomind.quickapp.generate.GenerateFixtures;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.Callable;
import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@CommandLine.Command(
        name = "test",
        mixinStandardHelpOptions = true,
        description = "test template"
)
public class TestCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "template directory or name (<repoName>/<templateName>[:version])", defaultValue = "./")
    private String templateDirOrName;

    @ParentCommand
    Main parent;

    @SneakyThrows
    @Override
    public Integer call() throws Exception {
        var outputDir = Files.createTempDirectory("quickapp-test").toFile();

        File result;
        if (isTemplateRef(templateDirOrName)) {
            var templateRef = parse(templateDirOrName);

            var repoCache = parent.getConfig()
                    .getRepoList()
                    .getRepositories()
                    .stream()
                    .filter(r -> r.getName().equals(templateRef.getRepository()))
                    .findFirst()
                    .map(parent.getConfig()::getRepoCache)
                    .orElseThrow(
                            () -> new RuntimeException("Repository " + templateRef.getRepository()
                                    + " not found."));
            var tmpExtractionDir = extractTemplate(
                    repoCache.getTemplatePackageFile(templateRef.getName(),
                            templateRef.getVersion()));
            result = new File(tmpExtractionDir, templateRef.getName());
        } else {
            result = new File(templateDirOrName);
        }
        var manifest = GenerateFixtures.doGenerate(result, outputDir, true, null,
                new HashMap<>()
        );

        if (manifest.getValidator() == null) {
            System.out.println("No validator found for template " + manifest.getName() + ".");
        } else {
            String command = "docker run "
                    + " -v " + outputDir.getAbsolutePath() + ":/opt/qa-test "
                    + " --workdir /opt/qa-test "
                    + " " + manifest.getValidator().getImage() + " "
                    + " " + manifest.getValidator().getCommand() + " ";

            if (execute(command) != 0) {
                throw new RuntimeException("Error when validating template: " + command);
            }
        }
        return 0;
    }
}
