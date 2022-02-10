package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;

public class ProjectViewBazelPathSectionParser
    extends ProjectViewSingletonSectionParser<ProjectViewBazelPathSection> {

  public ProjectViewBazelPathSectionParser() {
    super(ProjectViewBazelPathSection.SECTION_NAME);
  }

  @Override
  protected ProjectViewBazelPathSection createInstance(String value) {
    return new ProjectViewBazelPathSection(value);
  }
}
