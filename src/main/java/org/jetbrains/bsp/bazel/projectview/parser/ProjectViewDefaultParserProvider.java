package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import java.io.File;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewDefaultProvider;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;

public class ProjectViewDefaultParserProvider implements ProjectViewProvider {

  private final ProjectViewParser parser;
  private final File projectViewFile;

  private final ProjectViewProvider defaultProvider;

  public ProjectViewDefaultParserProvider() {
    this.parser = new ProjectViewParserImpl();
    this.projectViewFile = new File(Constants.DEFAULT_PROJECT_VIEW_FILE);
    this.defaultProvider = new ProjectViewDefaultProvider();
  }

  @Override
  public ProjectView create() {
    return Try.success(projectViewFile)
        .filter(File::isFile)
        .mapTry(parser::parse)
        .getOrElse(defaultProvider::create);
  }
}
