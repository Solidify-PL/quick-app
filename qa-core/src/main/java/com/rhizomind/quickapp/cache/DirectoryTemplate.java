package com.rhizomind.quickapp.cache;

import com.rhizomind.quickapp.Commons;
import com.rhizomind.quickapp.GenerateFixtures;
import com.rhizomind.quickapp.model.Manifest;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectoryTemplate implements Template {

    private final File templateDir;

    @Override
    public Manifest getManifest() throws IOException {
        return Commons.loadManifest(new File(templateDir, "manifest.yaml"));
    }

    @Override
    public Map<String, String> getDefaults() throws IOException {
        var manifest = getManifest();
        return GenerateFixtures.loadMapParameters(new File(templateDir, manifest.getValues().getDefaults()));
    }
}
