package com.rhizomind.quickapp.generate.command;

import static com.rhizomind.quickapp.generate.TemplateRef.isTemplateRef;
import static com.rhizomind.quickapp.generate.TemplateRef.parse;

import com.rhizomind.quickapp.Commons;
import com.rhizomind.quickapp.DirectoryTemplate;
import com.rhizomind.quickapp.Main;
import com.rhizomind.quickapp.Template;
import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@CommandLine.Command(
        name = "describe",
        mixinStandardHelpOptions = true,
        description = "Packages working directory (if it's a proper template) into tar.gz file"
)
public class DescribeCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "template directory or name (<repoName>/<templateName>[:version])")
    private String templateDirOrName;

    @ParentCommand
    Main parent;

    @Override
    public Integer call() throws Exception {
        Template template = null;
        if (isTemplateRef(templateDirOrName)) {
            var templateRef = parse(templateDirOrName);
            template = parent.getConfig().getRepoCache(templateRef.getRepository())
                    .orElseThrow(() -> new RuntimeException(
                            "Repository " + templateRef.getRepository() + " not found"))
                    .getTemplate(templateRef.getName(), templateRef.getVersion());
        } else {
            template = new DirectoryTemplate(new File(templateDirOrName));
        }
        var manifest = template.getManifest();
        System.out.println(Commons.OBJECT_MAPPER.writeValueAsString(manifest));

        var defaults = template.getDefaults();
        System.out.println(Commons.OBJECT_MAPPER.writeValueAsString(defaults));

        return 0;
    }
}
