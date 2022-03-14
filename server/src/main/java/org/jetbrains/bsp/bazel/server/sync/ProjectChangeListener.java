package org.jetbrains.bsp.bazel.server.sync;

import org.jetbrains.bsp.bazel.server.sync.model.Project;

interface ProjectChangeListener {
  void onProjectChange(Project project);
}
