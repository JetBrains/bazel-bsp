package org.jetbrains.bsp.bazel.projectview.model;

import io.vavr.control.Try;

public interface ProjectViewProvider {

  Try<ProjectView> create();
}
