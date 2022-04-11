package org.jetbrains.bsp.bazel.projectview.parser.sections;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;

public class ProjectViewBazelPathSectionParser
    extends ProjectViewSingletonSectionParser<Path, ProjectViewBazelPathSection> {

  public ProjectViewBazelPathSectionParser() {
    // TODO
    super("bazel_path");
  }

  @Override
  protected Path mapRawValue(String rawValue) {
    return Paths.get(rawValue);
  }

  @Override
  protected ProjectViewBazelPathSection createInstance(Path value) {
    return new ProjectViewBazelPathSection(value);
  }
}
