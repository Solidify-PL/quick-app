package com.rhizomind.quickapp;

import com.rhizomind.quickapp.model.Generator;
import com.rhizomind.quickapp.model.GeneratorConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DockerGeneratorConfig extends GeneratorConfig {

    private String image;
    private List<String> args;

    @Override
    public Generator createGenerator() {
        return new DockerGenerator(this);
    }
}
