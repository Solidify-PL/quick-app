package com.rhizomind.quickapp;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.reflect.ReflectionObjectHandler;
import com.rhizomind.quickapp.model.Generator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MustacheGenerator implements Generator {

    private final DefaultMustacheFactory mf4Paths = new DefaultMustacheFactory();
    private final DefaultMustacheFactory mf4Files = new DefaultMustacheFactory();

    private final MustacheGeneratorConfig config;

    public MustacheGenerator(MustacheGeneratorConfig config) {
        this.config = config;
        this.mf4Paths.setObjectHandler(new ReflectionObjectHandler() {

            @Override
            public String stringify(Object object) {
                return super.stringify(object)
                        .replace(".", File.separator);
            }
        });
    }

    @Override
    public void generate(Path srcBasePath, Path dstBasePath, Map<String, String> values) throws Exception {
        Files.walk(srcBasePath)
                .filter(item -> !item.equals(srcBasePath))
                .forEach(srcPath -> {
                    try {
                        Path dstPath = calculateDstPath(srcBasePath, dstBasePath, values, srcPath);

                        File dstFile = dstPath.toFile();
                        dstFile.getParentFile().mkdirs();

                        if (srcPath.toFile().isFile()) {
                            dstFile.createNewFile();
                            boolean justCopy = matchesAnyOfExclusionPatterns(dstBasePath.relativize(dstPath).toString(), config.getExcludeRegExp());
                            System.out.println("(" + (justCopy ? 'C' : 'P') + ") '" + srcPath + "' -> '" + dstPath + "'");

                            if (justCopy) {
                                copyFile(srcPath, dstPath);
                            } else {
                                processFile(srcPath, dstPath, values);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("unable to transport file to destination location: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    private boolean matchesAnyOfExclusionPatterns(String value, Set<String> patterns) {
        if (patterns.isEmpty())
            return false;

        Boolean aBoolean = patterns.stream()
                .map(pattern -> Pattern.compile(pattern).matcher(value).matches())
                .filter(a -> a)
                .findFirst()
                .orElse(false);
        return aBoolean;
    }

    private Path calculateDstPath(Path srcBasePath, Path dstBasePath, Map<String, String> parameters, Path srcPath) {
        String target = executeMustache(srcBasePath.relativize(srcPath).toString(), parameters);
        return dstBasePath.resolve(
                target
        );
    }

    private String executeMustache(String value, Map<String, String> parameters) {
        var outBaos = new ByteArrayOutputStream();
        var writer = new OutputStreamWriter(outBaos);
        Mustache mustache = mf4Paths.compile(new StringReader(value), "example");
        mustache.execute(writer, parameters);
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(outBaos.toByteArray());
    }

    private void copyFile(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING, COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void processFile(Path source, Path dest, Map<String, String> parameters) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING, COPY_ATTRIBUTES);
            try (FileReader reader = new FileReader(source.toFile()); FileWriter writer = new FileWriter(dest.toFile())) {
                Mustache mustache = mf4Files.compile(reader, "example");
                mustache.execute(writer, parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
