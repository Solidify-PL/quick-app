package com.rhizomind.quickapp.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rhizomind.quickapp.DockerGeneratorConfig;
import com.rhizomind.quickapp.MustacheGeneratorConfig;
import lombok.Data;

@Data

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MustacheGeneratorConfig.class, name = "mustache"),
        @JsonSubTypes.Type(value = DockerGeneratorConfig.class, name = "docker")
})
public abstract class GeneratorConfig {

    public abstract Generator createGenerator();
}
