package com.rhizomind.quickapp.build.command;

import com.rhizomind.quickapp.Commons;
import com.rhizomind.quickapp.model.GeneratorConfig;
import com.rhizomind.quickapp.model.Manifest;
import com.rhizomind.quickapp.model.ValidatorConfig;
import com.rhizomind.quickapp.model.ValuesConfig;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;


@CommandLine.Command(
        name = "init",
        mixinStandardHelpOptions = true,
        description = "Initializes working directory with a template structure"
)
public class InitCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-d",
            "--dir"}, description = "directory (Workdir by default)")
    private File dir;


    @Override
    public Integer call() throws Exception {
        var templateDir = this.dir == null ? new File(".") : this.dir;

        if (templateDir.exists() && !templateDir.isDirectory()) {
            throw new IllegalArgumentException(
                    "Template directory '" + templateDir.getAbsolutePath() + "' is not a directory");
        }

        File filesDir = new File(templateDir, "files");
        File file = new File(filesDir, "example.txt");
        File defaultsFile = new File(templateDir, "parameters-defaults.yaml");
        File manifestFile = new File(templateDir, "manifest.yaml");
        File schemaFile = new File(templateDir, "parameters-schema.json");

        if (!filesDir.mkdirs()) {
            throw new RuntimeException("Failed to create files directory");
        }
        if (!defaultsFile.createNewFile()) {
            throw new RuntimeException("Failed to create defaults file");
        }
        if (!manifestFile.createNewFile()) {
            throw new RuntimeException("Failed to create manifest file");
        }
        if (!schemaFile.createNewFile()) {
            throw new RuntimeException("Failed to create schema file");
        }
        if (!file.createNewFile()) {
            throw new RuntimeException("Failed to create example file");
        }

        Manifest manifest = new Manifest();
        manifest.setName("template-name");
        manifest.setVersion("1.0.0");
        manifest.setTags(List.of("tag1", "tag2"));
        manifest.setDescription("Template description");

        manifest.setValues(new ValuesConfig());
        manifest.getValues().setDefaults("parameters-defaults.yaml");
        manifest.getValues().setSchema("parameters-schema.json");

        manifest.setValidator(new ValidatorConfig());
        manifest.getValidator().setImage("node:22-alpine");
        manifest.getValidator().setArgs(List.of("--entrypoint sh"));
        manifest.getValidator().setCommand("-c \"npm install && npm test\"");
        manifest.setGenerator(new GeneratorConfig());
        manifest.getGenerator().setImage("solidify-qa-docker.dspr.deploy-sphere.cloud/quick-app/mustache-cli:latest");
        manifest.getGenerator().setArgs(List.of("--exclude=\".*(jar|png|svg|ico)$\"", "--exclude=\"\\.git\\/.*$\""));

        String defaultSchema = """
                {
                  "$schema": "http://json-schema.org/draft-07/schema#",
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string",
                      "title": "Id of the artifact; name of a .jar file and dockerimage"
                    }
                  },
                  "required": [
                    "name"
                  ]
                }
                """;
        String defaults = """
                name: template-name
                """;
        FileUtils.write(schemaFile, defaultSchema, UTF_8);
        FileUtils.write(defaultsFile, defaults, UTF_8);
        FileUtils.write(file, "{{name}}", UTF_8);
        Commons.OBJECT_MAPPER.writeValue(manifestFile, manifest);

        return 0;
    }
}
