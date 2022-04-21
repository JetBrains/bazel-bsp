package org.jetbrains.bsp.bazel.installationcontext;

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;

public class InstallationContext extends ExecutionContext {

  private final InstallationContextJavaPathEntity javaPath;
  private final Option<InstallationContextDebuggerAddressEntity> debuggerAddress;

  private final Option<Path> projectViewFilePath;

  private InstallationContext(
      InstallationContextJavaPathEntity javaPath,
      Option<InstallationContextDebuggerAddressEntity> debuggerAddress,
      Option<Path> projectViewFilePath) {
    this.javaPath = javaPath;
    this.debuggerAddress = debuggerAddress;
    this.projectViewFilePath = projectViewFilePath;
  }

  public static Builder builder() {
    return new Builder();
  }

  public InstallationContextJavaPathEntity getJavaPath() {
    return javaPath;
  }

  public Option<InstallationContextDebuggerAddressEntity> getDebuggerAddress() {
    return debuggerAddress;
  }

  public Option<Path> getProjectViewFilePath() {
    return projectViewFilePath;
  }

  public static class Builder {

    private Option<InstallationContextJavaPathEntity> javaPath = Option.none();
    private Option<InstallationContextDebuggerAddressEntity> debuggerAddress = Option.none();
    private Option<Path> projectViewFilePath = Option.none();

    private Builder() {}

    public Builder javaPath(InstallationContextJavaPathEntity javaPath) {
      this.javaPath = Option.of(javaPath);
      return this;
    }

    public Builder debuggerAddress(
        Option<InstallationContextDebuggerAddressEntity> debuggerAddress) {
      this.debuggerAddress = debuggerAddress;
      return this;
    }

    public Builder projectViewFilePath(Option<Path> projectViewFilePath) {
      this.projectViewFilePath = projectViewFilePath;
      return this;
    }

    public Try<InstallationContext> build() {
      if (javaPath.isEmpty()) {
        var exceptionMessage =
            "Installation context creation failed! 'javaPath' has to be defined.";
        return Try.failure(new IllegalStateException(exceptionMessage));
      }
      return Try.success(
          new InstallationContext(javaPath.get(), debuggerAddress, projectViewFilePath));
    }
  }
}
