package com.rhizomind.quickapp.build.command;

import com.rhizomind.quickapp.BuildFixtures;
import picocli.CommandLine;

import java.io.*;
import java.util.concurrent.Callable;

import static com.rhizomind.quickapp.BuildFixtures.isValidTemplateDir;

@CommandLine.Command(
        name = "package",
        mixinStandardHelpOptions = true,
        description = "Packages working directory (if it's a proper template) into tar.gz file"
)
public class PackageCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-t",
            "--template"}, description = "template directory (Workdir by default)")
    private File templateDir;

    @CommandLine.Option(names = {"-o",
            "--output"}, description = "Output directory (Workdir by default) ")
    private File outputDir;

    @Override
    public Integer call() throws Exception {
        var templateDir = this.templateDir == null ? new File(".") : this.templateDir;
        var outputDir = this.outputDir == null ? new File(".") : this.outputDir;

        if (isValidTemplateDir(templateDir)) {
            BuildFixtures.doPackage(templateDir, outputDir);
        } else {
            throw new RuntimeException("Invalid template directory: " + templateDir.getAbsolutePath());
        }
        return 0;
    }

}
