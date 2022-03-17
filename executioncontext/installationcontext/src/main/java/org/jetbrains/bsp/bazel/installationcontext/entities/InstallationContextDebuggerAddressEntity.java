package org.jetbrains.bsp.bazel.installationcontext.entities;

import com.google.common.net.HostAndPort;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextSingletonEntity;

public class InstallationContextDebuggerAddressEntity
    extends ExecutionContextSingletonEntity<HostAndPort> {

  public InstallationContextDebuggerAddressEntity(HostAndPort value) {
    super(value);
  }

  @Override
  public String toString() {
    return "InstallationContextDebuggerAddressEntity{" + "value=" + value + '}';
  }
}
