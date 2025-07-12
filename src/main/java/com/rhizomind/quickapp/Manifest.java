package com.rhizomind.quickapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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


    public Manifest() {
        tags = new ArrayList<>();
        values = new ValuesConfig();
    }
}
