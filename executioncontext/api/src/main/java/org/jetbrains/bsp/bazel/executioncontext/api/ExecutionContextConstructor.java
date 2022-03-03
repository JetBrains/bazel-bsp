package org.jetbrains.bsp.bazel.executioncontext.api;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public interface ExecutionContextConstructor<T extends ExecutionContext> {

  default Try<T> construct(Try<ProjectView> projectViewTry) {
    return projectViewTry.flatMap(this::construct);
  }

  Try<T> construct(ProjectView projectView);
}
