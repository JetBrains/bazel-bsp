package org.jetbrains.bsp.bazel.projectview.parser;

import java.nio.file.Path;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;

public class ProjectViewDefaultParserProvider implements ProjectViewProvider {

  private static final ProjectViewParser PARSER = new ProjectViewParserImpl();

  private final Path projectViewFile;

  public ProjectViewDefaultParserProvider(Path bspProjectRoot) {
    this.projectViewFile = bspProjectRoot.resolve(Constants.DEFAULT_PROJECT_VIEW_FILE);
  }

  @Override
  public ProjectView create() {
    return null;
  }
}
