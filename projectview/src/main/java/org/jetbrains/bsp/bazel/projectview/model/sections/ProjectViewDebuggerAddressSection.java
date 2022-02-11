package org.jetbrains.bsp.bazel.projectview.model.sections;

public class ProjectViewDebuggerAddressSection extends ProjectViewSingletonSection {

  public static final String SECTION_NAME = "debugger_address";

  public ProjectViewDebuggerAddressSection(String value) {
    super(SECTION_NAME, value);
  }

  @Override
  public String toString() {
    return "ProjectViewDebuggerAddressSection{" + "value=" + value + "} ";
  }
}
