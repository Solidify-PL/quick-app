package com.rhizomind.quickapp.build;

import com.rhizomind.quickapp.Manifest;
import com.rhizomind.quickapp.generate.GenerateFixtures;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.rhizomind.quickapp.common.Process.execute;

@CommandLine.Command(
        name = "test",
        mixinStandardHelpOptions = true,
        description = "test template"
)
public class TestCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-t",
            "--template"}, description = "template directory (Workdir by default)")
    private File templateDir;

    @Override
    public Integer call() throws Exception {
        var templateDir = this.templateDir == null ? new File(".") : this.templateDir;
        var outputDir = Files.createTempDirectory("quickapp-test").toFile();

        Manifest manifest = GenerateFixtures.doGenerate(
                templateDir.getAbsolutePath(),
                outputDir,
                true, null, new HashMap<>()
        );

        String command = "docker run "
                + " -v " + outputDir.getAbsolutePath() + ":/opt/qa-test "
                + " --workdir /opt/qa-test "
                + " " + manifest.getValidator().getImage() + " "
                + " " + manifest.getValidator().getCommand() + " ";

        if (execute(command) != 0) {
            throw new RuntimeException("Error when validating template: " + command);
        }
        return 0;
    }
}
