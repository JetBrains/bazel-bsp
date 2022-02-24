package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;

public class ProjectViewJavaPathSectionParser
    extends ProjectViewSingletonSectionParser<String, ProjectViewJavaPathSection> {

  public ProjectViewJavaPathSectionParser() {
    super(ProjectViewJavaPathSection.SECTION_NAME);
  }

  @Override
  protected String mapRawValue(String rawValue) {
    return rawValue;
  }

  @Override
  protected ProjectViewJavaPathSection createInstance(String value) {
    return new ProjectViewJavaPathSection(value);
  }
}
