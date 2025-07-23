package com.rhizomind.quickapp.model;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class Manifest {

    private String name;
    private String version;
    private String description;
    @Singular
    private List<String> tags;
    private GeneratorConfig generator;
    private ValuesConfig values;
    private ValidatorConfig validator;


    public Manifest() {
        tags = new ArrayList<>();
        values = new ValuesConfig();
    }
}
