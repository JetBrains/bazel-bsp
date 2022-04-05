package org.jetbrains.bsp.bazel.install;

import io.vavr.control.Try;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.commons.BetterFiles;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParserImpl;

// TODO @abrams27 I'll take care of that later
public class ProjectViewDefaultFromResourcesProvider implements ProjectViewProvider {

  private static final ProjectViewParser PARSER = new ProjectViewParserImpl();

  private final Path projectViewFile;
  private final String defaultProjectViewFileResources;

  public ProjectViewDefaultFromResourcesProvider(
      Path projectViewFile, String defaultProjectViewFileResources) {
    this.projectViewFile = projectViewFile;
    this.defaultProjectViewFileResources = defaultProjectViewFileResources;
  }

  @Override
  public Try<ProjectView> create() {
    return readFileContentFromResources(defaultProjectViewFileResources)
        .peek(System.out::println)
        .flatMap(this::create);
  }

  private Try<String> readFileContentFromResources(String resourcesRawPath) {
    // TODO add https://youtrack.jetbrains.com/issue/BAZEL-18
    return Try.of(() -> ProjectViewDefaultFromResourcesProvider.class.getResourceAsStream(resourcesRawPath))
            .map(InputStreamReader::new)
            .map(BufferedReader::new)
            .mapTry(BufferedReader::lines)
            .map(stream -> stream.collect(Collectors.joining("\n")));
  }

  private Try<ProjectView> create(String defaultProjectViewFileContent) {
    return BetterFiles.tryReadFileContent(projectViewFile)
        .flatMap(
            projectViewFileContent ->
                PARSER.parse(projectViewFileContent, defaultProjectViewFileContent))
        .orElse(PARSER.parse(defaultProjectViewFileContent));
  }
}
