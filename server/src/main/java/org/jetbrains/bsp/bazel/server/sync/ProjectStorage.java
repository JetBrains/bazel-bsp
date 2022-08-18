package org.jetbrains.bsp.bazel.server.sync;

import org.jetbrains.bsp.bazel.server.sync.model.Project;

public interface ProjectStorage {
  Project load(String originId);

  void store(Project project, String originId);
}
