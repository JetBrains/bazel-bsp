package org.jetbrains.bsp.bazel.install;

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.commons.BetterFiles;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParserImpl;

// TODO @abrams27 I'll take care of that later
public class ProjectViewDefaultFromResourcesProvider implements ProjectViewProvider {

  private static final ProjectViewParser PARSER = new ProjectViewParserImpl();

  private final Option<Path> projectViewFilePath;
  private final String defaultProjectViewFileResourcesPath;

  public ProjectViewDefaultFromResourcesProvider(
      Option<Path> projectViewFilePath, String defaultProjectViewFileResourcesPath) {
    this.projectViewFilePath = projectViewFilePath;
    this.defaultProjectViewFileResourcesPath = defaultProjectViewFileResourcesPath;
  }

  @Override
  public Try<ProjectView> create() {
    return readFileContentFromResources(defaultProjectViewFileResourcesPath).flatMap(this::create);
  }

  private Try<String> readFileContentFromResources(String resourcesRawPath) {
    // TODO add https://youtrack.jetbrains.com/issue/BAZEL-18
    return Try.of(
            () ->
                ProjectViewDefaultFromResourcesProvider.class.getResourceAsStream(resourcesRawPath))
        .map(InputStreamReader::new)
        .map(BufferedReader::new)
        .mapTry(BufferedReader::lines)
        .map(stream -> stream.collect(Collectors.joining("\n")));
  }

  private Try<ProjectView> create(String defaultProjectViewFileContent) {
    return projectViewFilePath
        .map(projectViewFile -> create(projectViewFile, defaultProjectViewFileContent))
        .getOrElse(() -> PARSER.parse(defaultProjectViewFileContent));
  }

  private Try<ProjectView> create(Path projectViewFilePath, String defaultProjectViewFileContent) {
    return BetterFiles.tryReadFileContent(projectViewFilePath)
        .flatMap(
            projectViewFileContent ->
                PARSER.parse(projectViewFileContent, defaultProjectViewFileContent))
        .orElse(() -> PARSER.parse(defaultProjectViewFileContent));
  }
}
