package org.jetbrains.bsp.bazel.projectview.model.sections;

import com.google.common.net.HostAndPort;

public class ProjectViewDebuggerAddressSection extends ProjectViewSingletonSection<HostAndPort> {

  public static final String SECTION_NAME = "debugger_address";

  public ProjectViewDebuggerAddressSection(HostAndPort value) {
    super(SECTION_NAME, value);
  }

  @Override
  public String toString() {
    return "ProjectViewDebuggerAddressSection{" + "value=" + value + "} ";
  }
}
