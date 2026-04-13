package com.rhizomind.quickapp;

import picocli.CommandLine;

import java.io.IOException;
import java.util.Properties;

public class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("version.properties"));
        return new String[]{properties.getProperty("version")};
    }
}
