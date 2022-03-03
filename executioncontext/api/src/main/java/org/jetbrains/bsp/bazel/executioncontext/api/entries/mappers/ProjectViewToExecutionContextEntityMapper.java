package org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

/**
 * Maps <code>ProjectView</code> into <code>ExecutionContextEntity</code>. It takes entire <code>
 * ProjectView</code> because sometimes in order to create one <code>ExecutionContextEntity</code>
 * you may want to use multiple <code>ProjectView</code> sections.
 *
 * @param <T> type of the mapped entity
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 * @see org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextEntity
 */
public interface ProjectViewToExecutionContextEntityMapper<T extends ExecutionContextEntity> {

  Try<T> map(ProjectView projectView);
}
