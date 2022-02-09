package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.commons.BetterFiles;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

/**
 * Project view file parser. Its purpose is to parse *.bazelproject file and create ProjectView
 *
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
public interface ProjectViewParser {

  default Try<ProjectView> parse(Path projectViewFilePath, Path defaultProjectViewFilePath) {
    return BetterFiles.tryReadFileContent(defaultProjectViewFilePath)
        .flatMap(
            defaultProjectViewFileContent ->
                parseWithDefault(projectViewFilePath, defaultProjectViewFileContent));
  }

  private Try<ProjectView> parseWithDefault(
      Path projectViewFilePath, String defaultProjectViewFileContent) {
    return BetterFiles.tryReadFileContent(projectViewFilePath)
        .flatMap(
            projectViewFilePathContent ->
                parse(projectViewFilePathContent, defaultProjectViewFileContent))
        .orElse(parse(defaultProjectViewFileContent));
  }

  Try<ProjectView> parse(String projectViewFileContent, String defaultProjectViewFileContent);

  default Try<ProjectView> parse(Path projectViewFilePath) {
    return BetterFiles.tryReadFileContent(projectViewFilePath).flatMap(this::parse);
  }

  Try<ProjectView> parse(String projectViewFileContent);
}
