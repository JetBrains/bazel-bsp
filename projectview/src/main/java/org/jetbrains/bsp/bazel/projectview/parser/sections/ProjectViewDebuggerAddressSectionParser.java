package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;

public class ProjectViewDebuggerAddressSectionParser
    extends ProjectViewSingletonSectionParser<String, ProjectViewDebuggerAddressSection> {

  public ProjectViewDebuggerAddressSectionParser() {
    super(ProjectViewDebuggerAddressSection.SECTION_NAME);
  }

  @Override
  protected String mapRawValue(String rawValue) {
    return rawValue;
  }

  @Override
  protected ProjectViewDebuggerAddressSection createInstance(String value) {
    return new ProjectViewDebuggerAddressSection(value);
  }
}
