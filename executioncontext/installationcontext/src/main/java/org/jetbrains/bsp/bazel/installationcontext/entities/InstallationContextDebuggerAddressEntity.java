package org.jetbrains.bsp.bazel.installationcontext.entities;

import org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextSingletonEntity;

public class InstallationContextDebuggerAddressEntity
    extends ExecutionContextSingletonEntity<String> {

  public InstallationContextDebuggerAddressEntity(String value) {
    super(value);
  }

  @Override
  public String toString() {
    return "InstallationContextDebuggerAddressEntity{" + "value=" + value + '}';
  }
}
