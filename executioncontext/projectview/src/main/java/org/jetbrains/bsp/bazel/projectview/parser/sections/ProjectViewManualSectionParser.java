package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewManualSection;

public class ProjectViewManualSectionParser
    extends ProjectViewSingletonSectionParser<Boolean, ProjectViewManualSection> {

  public ProjectViewManualSectionParser() {
    // TODO
    super("build_manual_targets");
  }

  @Override
  protected Boolean mapRawValue(String rawValue) {
    return Boolean.valueOf(rawValue);
  }

  @Override
  protected ProjectViewManualSection createInstance(Boolean value) {
    return new ProjectViewManualSection(value);
  }
}
