package com.rhizomind.quickapp.cache;

import java.util.regex.Pattern;
import lombok.Value;

@Value
public class TemplateRef {

    String repository;
    String name;
    String version;

    private static final Pattern TEMPLATE_REF_PATTERN =
            Pattern.compile("^[^/\\\\:]+/[^/\\\\:]+(:[^/\\\\:]+)?$");

    public static boolean isTemplateRef(String templateDirOrName) {
        return TEMPLATE_REF_PATTERN.matcher(templateDirOrName).matches();
    }

    public static TemplateRef parse(String input) {
        if (input == null || !input.contains("/")) {
            throw new IllegalArgumentException(
                    "Invalid format: expected <repo>/<template>[:version]");
        }

        String[] repoAndRest = input.split("/", 2);
        String repo = repoAndRest[0];
        String templateAndMaybeVersion = repoAndRest[1];

        String[] templateAndVersion = templateAndMaybeVersion.split(":", 2);
        if (templateAndVersion.length == 2) {
            return new TemplateRef(repo, templateAndVersion[0], templateAndVersion[1]);
        } else {
            return new TemplateRef(repo, templateAndVersion[0], null);
        }
    }

}
