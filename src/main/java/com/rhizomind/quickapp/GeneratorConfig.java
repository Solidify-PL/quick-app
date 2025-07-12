package com.rhizomind.quickapp;

import java.util.List;
import lombok.Data;

@Data
public class GeneratorConfig {

    private String image;
    private List<String> args;

}
