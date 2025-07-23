package com.rhizomind.quickapp.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Repo {

    private String name;
    private URL url;

}
