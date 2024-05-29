package org.jetbrains.bsp.bazel.server.sync;

import org.jetbrains.bsp.bazel.server.model.Project;

public interface ProjectStorage {
  Project load();

  void store(Project project);
}
