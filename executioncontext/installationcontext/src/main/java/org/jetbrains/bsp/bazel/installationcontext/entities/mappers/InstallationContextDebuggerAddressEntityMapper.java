package org.jetbrains.bsp.bazel.installationcontext.entities.mappers;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityOptionMapper;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;

public class InstallationContextDebuggerAddressEntityMapper
    implements ProjectViewToExecutionContextEntityOptionMapper<
        InstallationContextDebuggerAddressEntity> {

  @Override
  public Try<Option<InstallationContextDebuggerAddressEntity>> map(ProjectView projectView) {
    var debuggerAddressEntity = Option.of(projectView.getDebuggerAddress()).map(this::map);

    return Try.success(debuggerAddressEntity);
  }

  private InstallationContextDebuggerAddressEntity map(
      ProjectViewDebuggerAddressSection debuggerAddressSection) {
    return new InstallationContextDebuggerAddressEntity(debuggerAddressSection.getValue());
  }
}
