package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;

@Deprecated
public class ProjectViewDefaultParserProvider implements ProjectViewProvider {

  private static final ProjectViewParser PARSER = new ProjectViewParserImpl();

  private final Path projectViewFile;
  private final Path defaultProjectViewFile;

  public ProjectViewDefaultParserProvider(Path bspProjectRoot) {
    this.projectViewFile = bspProjectRoot.resolve(Constants.PROJECT_VIEW_FILE_PATH);
    this.defaultProjectViewFile = bspProjectRoot.resolve(Constants.DEFAULT_PROJECT_VIEW_FILE_PATH);
  }

  @Override
  public Try<ProjectView> create() {
    return PARSER.parse(projectViewFile, defaultProjectViewFile);
  }
}
