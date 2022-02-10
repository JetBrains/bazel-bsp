package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;

public class ProjectViewJavaPathSectionParser
    extends ProjectViewSingletonSectionParser<ProjectViewJavaPathSection> {

  public ProjectViewJavaPathSectionParser() {
    super(ProjectViewJavaPathSection.SECTION_NAME);
  }

  @Override
  protected ProjectViewJavaPathSection createInstance(String value) {
    return new ProjectViewJavaPathSection(value);
  }
}
