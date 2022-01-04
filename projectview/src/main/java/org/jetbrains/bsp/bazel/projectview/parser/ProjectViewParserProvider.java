package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewDefaultProvider;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;

public class ProjectViewDefaultParserProvider implements ProjectViewProvider {

  private static final ProjectViewParser PARSER = new ProjectViewParserImpl();
  private static final ProjectViewProvider PROJECT_VIEW_PROVIDER = new ProjectViewDefaultProvider();

  private final Path projectViewFile;

  public ProjectViewDefaultParserProvider(Path bspProjectRoot) {
    this.projectViewFile = bspProjectRoot.resolve(Constants.DEFAULT_PROJECT_VIEW_FILE);
  }

  @Override
  public ProjectView create() {
    return Try.success(projectViewFile)
        .filter(Files::isRegularFile)
        .mapTry(PARSER::parse)
        .getOrElse(PROJECT_VIEW_PROVIDER::create);
  }
}
