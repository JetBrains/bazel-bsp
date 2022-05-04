package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewManualTargetsSection;

public class ProjectViewManualSectionParser
    extends ProjectViewSingletonSectionParser<Boolean, ProjectViewManualTargetsSection> {

  public ProjectViewManualSectionParser() {
    // TODO
    super("build_manual_targets");
  }

  @Override
  protected Boolean mapRawValue(String rawValue) {
    return Boolean.valueOf(rawValue);
  }

  @Override
  protected ProjectViewManualTargetsSection createInstance(Boolean value) {
    return new ProjectViewManualTargetsSection(value);
  }
}
