package com.rhizomind.quickapp;

import com.rhizomind.quickapp.model.Generator;
import com.rhizomind.quickapp.model.GeneratorConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
public class MustacheGeneratorConfig extends GeneratorConfig {

    private Set<String> excludeRegExp;

    @Override
    public Generator createGenerator() {
        return new MustacheGenerator(this);
    }
}
