package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;

public class ProjectViewDefaultParserProvider implements ProjectViewProvider {

  private static final ProjectViewParser PARSER = new ProjectViewParserImpl();

  private final Option<Path> projectViewFile;
  private final Path defaultProjectViewFile;

  public ProjectViewDefaultParserProvider(Path bspProjectRoot, Option<Path> projectViewFile) {
    this.projectViewFile = projectViewFile;
    this.defaultProjectViewFile = bspProjectRoot.resolve(Constants.DEFAULT_PROJECT_VIEW_FILE_PATH);
  }

  @Override
  public Try<ProjectView> create() {
    return projectViewFile
        .map(projectView -> PARSER.parse(projectView, defaultProjectViewFile))
        .getOrElse(() -> PARSER.parse(defaultProjectViewFile));
  }
}
