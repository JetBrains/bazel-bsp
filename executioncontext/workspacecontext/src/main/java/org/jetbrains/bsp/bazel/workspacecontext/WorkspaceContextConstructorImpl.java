package org.jetbrains.bsp.bazel.workspacecontext;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextConstructor;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.workspacecontext.entries.mappers.WorkspaceContextTargetsEntityMapper;

public class WorkspaceContextConstructorImpl
    implements ExecutionContextConstructor<WorkspaceContext> {

  private static final WorkspaceContextTargetsEntityMapper targetsMapper =
      new WorkspaceContextTargetsEntityMapper();

  @Override
  public Try<WorkspaceContext> construct(ProjectView projectView) {
    return Try.success(WorkspaceContext.builder())
        .flatMap(builder -> withTargets(builder, projectView))
        .flatMap(WorkspaceContext.Builder::build);
  }

  private Try<WorkspaceContext.Builder> withTargets(
      WorkspaceContext.Builder builder, ProjectView projectView) {
    return targetsMapper.map(projectView).map(builder::targets);
  }
}
