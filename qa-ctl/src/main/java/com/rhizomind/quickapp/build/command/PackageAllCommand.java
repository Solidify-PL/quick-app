package com.rhizomind.quickapp.build.command;

import com.rhizomind.quickapp.BuildFixtures;
import picocli.CommandLine;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "package-all",
        mixinStandardHelpOptions = true,
        description = "Packages working directory (if it's a proper template) into tar.gz file"
)
public class PackageAllCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-t",
            "--templates"}, description = "template directory (Workdir by default)")
    private File templatesDir;

    @CommandLine.Option(names = {"-o",
            "--output"}, description = "Output directory (Workdir by default) ")
    private File outputDir;

    @Override
    public Integer call() throws Exception {
        var templatesDir = this.templatesDir == null ? new File(".") : this.templatesDir;
        var outputDir = this.outputDir == null ? new File(".") : this.outputDir;

        Arrays.stream(templatesDir.listFiles())
                .forEach(templateDir -> packageTemplate(templateDir, outputDir));
        return 0;
    }

    private static void packageTemplate(File templateDir, File outputDir) {
        try {
            if (BuildFixtures.isValidTemplateDir(templateDir)) {
                BuildFixtures.doPackage(templateDir, outputDir);
            }
        } catch (Exception e) {
            System.err.println("Error packaging template " + templateDir.getAbsolutePath() + ": "
                    + e.getMessage());
        }
    }

}
