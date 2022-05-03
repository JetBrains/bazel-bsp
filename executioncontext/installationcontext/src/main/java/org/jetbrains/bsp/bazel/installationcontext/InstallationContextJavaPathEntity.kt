package org.jetbrains.bsp.bazel.installationcontext.entities;

import java.nio.file.Path;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextSingletonEntity;

public class InstallationContextJavaPathEntity extends ExecutionContextSingletonEntity<Path> {

  public InstallationContextJavaPathEntity(Path value) {
    super(value);
  }

  @Override
  public String toString() {
    return "InstallationContextJavaPathEntity{" + "value=" + value + '}';
  }
}
