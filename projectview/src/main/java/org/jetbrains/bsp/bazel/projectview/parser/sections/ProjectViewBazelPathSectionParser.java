package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;

public class ProjectViewBazelPathSectionParser
    extends ProjectViewSingletonSectionParser<String, ProjectViewBazelPathSection> {

  public ProjectViewBazelPathSectionParser() {
    super(ProjectViewBazelPathSection.SECTION_NAME);
  }

  @Override
  protected String mapRawValue(String rawValue) {
    return rawValue;
  }

  @Override
  protected ProjectViewBazelPathSection createInstance(String value) {
    return new ProjectViewBazelPathSection(value);
  }
}
