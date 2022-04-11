package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.net.HostAndPort;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;

public class ProjectViewDebuggerAddressSectionParser
    extends ProjectViewSingletonSectionParser<HostAndPort, ProjectViewDebuggerAddressSection> {

  public ProjectViewDebuggerAddressSectionParser() {
    // TODO
    super("debugger_address");
  }

  @Override
  protected HostAndPort mapRawValue(String rawValue) {
    return HostAndPort.fromString(rawValue);
  }

  @Override
  protected ProjectViewDebuggerAddressSection createInstance(HostAndPort value) {
    return new ProjectViewDebuggerAddressSection(value);
  }
}
