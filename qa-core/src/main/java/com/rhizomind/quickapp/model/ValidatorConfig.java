package com.rhizomind.quickapp.model;

import java.util.List;
import lombok.Data;

@Data
public class ValidatorConfig {

    private String image;
    private List<String> args;
    private String command;

}
