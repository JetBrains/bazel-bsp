package org.jetbrains.bsp.bazel.workspacecontext.entries.mappers;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapper;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapperException;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;

public class WorkspaceContextTargetsEntityMapper
    implements ProjectViewToExecutionContextEntityMapper<ExecutionContextTargetsEntity> {

  @Override
  public Try<ExecutionContextTargetsEntity> map(ProjectView projectView) {
    var targetsSection = projectView.getTargets();

    return Option.ofOptional(targetsSection)
        .map(this::map)
        .toTry(
            () ->
                new ProjectViewToExecutionContextEntityMapperException(
                    "targets", "'targets' section in project view is empty."))
        .flatMap(this::validate);
  }

  private ExecutionContextTargetsEntity map(ProjectViewTargetsSection targetsSection) {
    var includedValues = targetsSection.getIncludedValues();
    var excludedValues = targetsSection.getExcludedValues();

    return new ExecutionContextTargetsEntity(includedValues, excludedValues);
  }

  private Try<ExecutionContextTargetsEntity> validate(ExecutionContextTargetsEntity entity) {
    if (entity.getIncludedValues().isEmpty()) {
      return Try.failure(
          new ProjectViewToExecutionContextEntityMapperException(
              "targets", "'targets' section has no included targets."));
    }

    return Try.success(entity);
  }
}
