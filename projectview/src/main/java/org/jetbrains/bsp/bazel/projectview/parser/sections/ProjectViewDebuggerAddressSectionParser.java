package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;

public class ProjectViewDebuggerAddressSectionParser
    extends ProjectViewSingletonSectionParser<ProjectViewDebuggerAddressSection> {

  public ProjectViewDebuggerAddressSectionParser() {
    super(ProjectViewDebuggerAddressSection.SECTION_NAME);
  }

  @Override
  protected ProjectViewDebuggerAddressSection createInstance(String value) {
    return new ProjectViewDebuggerAddressSection(value);
  }
}
