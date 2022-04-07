package org.jetbrains.bsp.bazel.server.bsp.config;

import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public interface ProjectViewProvider {
  ProjectView currentProjectView();
}
