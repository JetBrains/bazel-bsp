package org.jetbrains.bsp.bazel.installationcontext;

import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;

public class InstallationContext extends ExecutionContext {

  private InstallationContextJavaPathEntity javaPath;
  private Option<InstallationContextDebuggerAddressEntity> debuggerAddress;

  public InstallationContextJavaPathEntity getJavaPath() {
    return javaPath;
  }

  public Option<InstallationContextDebuggerAddressEntity> getDebuggerAddress() {
    return debuggerAddress;
  }
}
