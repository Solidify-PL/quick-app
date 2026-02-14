package com.rhizomind.quickapp;

import com.rhizomind.quickapp.model.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.rhizomind.quickapp.Commons.OBJECT_MAPPER;
import static com.rhizomind.quickapp.Joiner.join;

@Slf4j
@RequiredArgsConstructor
public class DockerGenerator implements Generator {

    private final DockerGeneratorConfig config;

    @Override
    public void generate(Path srcBasePath, Path dstBasePath, Map<String, String> values) throws Exception {
        log.info("Generating " + srcBasePath);
        log.info("Using following values:");
        log.info(OBJECT_MAPPER.writeValueAsString(values));

        var tempValuesFile = Files.createTempFile("quickapp", "values").toFile();
        OBJECT_MAPPER.writeValue(tempValuesFile, values);
        var command = "docker run --rm -u $(id -u):$(id -g) "
                + " -v " + new File(srcBasePath.toFile(), "files").getAbsolutePath()
                + ":/tmp/quickapp/input:ro "
                + " -v " + tempValuesFile.getAbsolutePath()
                + ":/tmp/quickapp/values.yaml:ro "
                + " -v " + dstBasePath.toFile().getAbsolutePath() + ":/tmp/quickapp/output "
                + " " + config.getImage()
                + " --input=/tmp/quickapp/input "
                + " --values=/tmp/quickapp/values.yaml "
                + " --output=/tmp/quickapp/output "
                + join(config.getArgs());

        if (Process.execute(List.of(command)) != 0) {
            throw new RuntimeException("Error executing generator: " + command);
        }
    }
}
