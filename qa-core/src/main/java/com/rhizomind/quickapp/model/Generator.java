package com.rhizomind.quickapp.model;

import java.nio.file.Path;
import java.util.Map;

public interface Generator {

    void generate(Path srcBasePath, Path dstBasePath, Map<String, String> values) throws Exception;

}
