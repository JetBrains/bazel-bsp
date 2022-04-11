package org.jetbrains.bsp.bazel.workspacecontext.entries.mappers;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapper;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapperException;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;

public class WorkspaceContextTargetsEntityMapper
    implements ProjectViewToExecutionContextEntityMapper<ExecutionContextTargetsEntity> {

  private static final String NAME = "targets";

  @Override
  public Try<ExecutionContextTargetsEntity> map(ProjectView projectView) {
    var targetsSection = projectView.getTargets();

    return toTry(targetsSection).flatMap(this::validate).map(this::map);
  }

  private Try<ProjectViewTargetsSection> toTry(Option<ProjectViewTargetsSection> targetsSection) {
    // TODO will be changed after ProjectView transition into vavr
    return targetsSection.toTry(
        () ->
            new ProjectViewToExecutionContextEntityMapperException(
                NAME, "'targets' section in project view is empty."));
  }

  private ExecutionContextTargetsEntity map(ProjectViewTargetsSection targetsSection) {
    var includedValues = List.ofAll(targetsSection.getValues());
    var excludedValues = List.ofAll(targetsSection.getExcludedValues());

    return new ExecutionContextTargetsEntity(includedValues, excludedValues);
  }

  private Try<ProjectViewTargetsSection> validate(ProjectViewTargetsSection targetsSection) {
    if (targetsSection.getValues().isEmpty()) {
      return Try.failure(
          new ProjectViewToExecutionContextEntityMapperException(
              NAME, "'targets' section has no included targets."));
    }

    return Try.success(targetsSection);
  }
}
