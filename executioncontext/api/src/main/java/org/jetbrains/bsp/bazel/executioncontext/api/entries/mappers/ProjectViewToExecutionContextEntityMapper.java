package org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public interface ProjectViewToExecutionContextEntityMapper<T> {

    Try<T> map(ProjectView projectView);
}
