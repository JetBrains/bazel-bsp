package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectViewBazelPathSectionParser
    extends ProjectViewSingletonSectionParser<Path, ProjectViewBazelPathSection> {

  public ProjectViewBazelPathSectionParser() {
    super(ProjectViewBazelPathSection.SECTION_NAME);
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
