package com.rhizomind.quickapp;

import java.io.IOException;
import java.util.Map;

public interface Template {

    Manifest getManifest() throws IOException;

    Map<String, String> getDefaults() throws IOException;
}
