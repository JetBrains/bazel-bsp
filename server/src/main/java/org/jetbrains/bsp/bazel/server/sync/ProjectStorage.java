package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.server.sync.model.Project;

public interface ProjectStorage {
  Option<Project> load();

  void store(Project project);
}
