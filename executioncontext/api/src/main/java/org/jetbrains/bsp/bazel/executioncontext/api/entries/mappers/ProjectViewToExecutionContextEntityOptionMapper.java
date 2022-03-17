package org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers;

import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextEntity;

/**
 * Maps <code>ProjectView</code> into <code>Option<ExecutionContextEntity></code>. It takes entire
 * <code>
 * ProjectView</code> because sometimes in order to create one <code>Option<ExecutionContextEntity>
 * </code> you may want to use multiple <code>ProjectView</code> sections and the result might be
 * empty.
 *
 * @param <T> type of the mapped entity
 * @see
 *     org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityBaseMapper
 * @see org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextEntity
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
public interface ProjectViewToExecutionContextEntityOptionMapper<T extends ExecutionContextEntity>
    extends ProjectViewToExecutionContextEntityBaseMapper<Option<T>> {}
