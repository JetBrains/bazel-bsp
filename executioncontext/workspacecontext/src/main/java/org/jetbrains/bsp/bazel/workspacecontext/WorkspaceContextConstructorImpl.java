package org.jetbrains.bsp.bazel.workspacecontext;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextConstructor;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public class WorkspaceContextConstructorImpl
    implements ExecutionContextConstructor<WorkspaceContext> {

  @Override
  public Try<WorkspaceContext> construct(ProjectView projectView) {
    return null;
  }
}
