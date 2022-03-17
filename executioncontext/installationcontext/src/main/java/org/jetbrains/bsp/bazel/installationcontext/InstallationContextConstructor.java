package org.jetbrains.bsp.bazel.installationcontext;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextConstructor;
import org.jetbrains.bsp.bazel.installationcontext.entities.mappers.InstallationContextDebuggerAddressEntityMapper;
import org.jetbrains.bsp.bazel.installationcontext.entities.mappers.InstallationContextJavaPathEntityMapper;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public class InstallationContextConstructor
    implements ExecutionContextConstructor<InstallationContext> {

  private static final InstallationContextJavaPathEntityMapper javaPathMapper =
      new InstallationContextJavaPathEntityMapper();

  private static final InstallationContextDebuggerAddressEntityMapper debuggerAddressMapper =
      new InstallationContextDebuggerAddressEntityMapper();

  @Override
  public Try<InstallationContext> construct(ProjectView projectView) {
    return Try.success(InstallationContext.builder())
        .flatMap(builder -> withJavaPath(builder, projectView))
        .flatMap(builder -> withDebuggerAddress(builder, projectView))
        .flatMap(InstallationContext.Builder::build);
  }

  private Try<InstallationContext.Builder> withJavaPath(
      InstallationContext.Builder builder, ProjectView projectView) {
    return javaPathMapper.map(projectView).map(builder::javaPath);
  }

  private Try<InstallationContext.Builder> withDebuggerAddress(
      InstallationContext.Builder builder, ProjectView projectView) {
    return debuggerAddressMapper.map(projectView).map(builder::debuggerAddress);
  }
}
