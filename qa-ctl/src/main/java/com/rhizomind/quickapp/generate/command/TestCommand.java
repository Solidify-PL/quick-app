package com.rhizomind.quickapp.generate.command;

import static com.rhizomind.quickapp.Process.execute;
import static com.rhizomind.quickapp.cache.TemplateRef.isTemplateRef;
import static com.rhizomind.quickapp.cache.TemplateRef.parse;

import com.rhizomind.quickapp.GenerateFixtures;
import com.rhizomind.quickapp.Joiner;
import com.rhizomind.quickapp.Main;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
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

    @Override
    public Integer call() throws Exception {
        var outputDir = Files.createTempDirectory("quickapp-test").toFile();

        File result;
        if (isTemplateRef(templateDirOrName)) {
            var templateRef = parse(templateDirOrName);

            var template = parent.getConfig()
                    .getRepoCache(templateRef.getRepository())
                    .orElseThrow(() -> new RuntimeException(
                            "Repository " + templateRef.getRepository() + " not found."))
                    .getTemplate(templateRef.getName(), templateRef.getVersion());
            result = new File(template.extractTemplate(), templateRef.getName());
        } else {
            result = new File(templateDirOrName);
        }
        var manifest = GenerateFixtures.doGenerate(result, outputDir, true, null,
                new HashMap<>()
        );

        if (manifest.getValidator() == null) {
            System.out.println("No validator found for template " + manifest.getName() + ".");
        } else {
            String command = "docker run --rm "
                    + " " + Joiner.join(manifest.getValidator().getArgs()) + " "
                    + " -v " + outputDir.getAbsolutePath() + ":/opt/qa-test "
                    + " --workdir /opt/qa-test "
                    + " " + manifest.getValidator().getImage() + " "
                    + " " + manifest.getValidator().getCommand() + " ";

            if (execute(List.of(command)) != 0) {
                throw new RuntimeException("Error when validating template: " + command);
            }
        }
        return 0;
    }
}
