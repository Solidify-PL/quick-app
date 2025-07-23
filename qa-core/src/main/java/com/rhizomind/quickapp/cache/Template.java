package com.rhizomind.quickapp.cache;

import com.rhizomind.quickapp.model.Manifest;
import java.io.IOException;
import java.util.Map;

public interface Template {

    Manifest getManifest() throws IOException;

    Map<String, String> getDefaults() throws IOException;
}
