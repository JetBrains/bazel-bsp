package org.jetbrains.bsp.bazel.projectview.parser.sections;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;

public class ProjectViewJavaPathSectionParser
    extends ProjectViewSingletonSectionParser<Path, ProjectViewJavaPathSection> {

  public ProjectViewJavaPathSectionParser() {
    super(ProjectViewJavaPathSection.SECTION_NAME);
  }

  @Override
  protected Path mapRawValue(String rawValue) {
    return Paths.get(rawValue);
  }

  @Override
  protected ProjectViewJavaPathSection createInstance(Path value) {
    return new ProjectViewJavaPathSection(value);
  }
}
