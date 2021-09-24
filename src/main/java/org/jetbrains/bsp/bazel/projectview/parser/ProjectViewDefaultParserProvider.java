package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewDefaultProvider;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;

public class ProjectViewDefaultParserProvider implements ProjectViewProvider {

  private static final ProjectViewParser PARSER = new ProjectViewParserImpl();
  private static final Path PROJECT_VIEW_FILE = Paths.get(Constants.DEFAULT_PROJECT_VIEW_FILE);
  private static final ProjectViewProvider PROJECT_VIEW_PROVIDER = new ProjectViewDefaultProvider();

  @Override
  public ProjectView create() {
    return Try.success(PROJECT_VIEW_FILE)
        .filter(this::doesProjectViewFileExists)
        .mapTry(PARSER::parse)
        .getOrElse(PROJECT_VIEW_PROVIDER::create);
  }

  private boolean doesProjectViewFileExists(Path projectViewFile) {
    return projectViewFile.toFile().isFile();
  }
}
