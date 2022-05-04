package org.jetbrains.bsp.bazel.executioncontext.api

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.ProjectView

/**
 * `ExecutionContext` base class - you need to extend it if you want to create your
 * implementation of `ExecutionContext`.
 */
abstract class ExecutionContext

/**
 * Constructs a `ExecutionContext` for a `ProjectView`. Probably you should use
 * `ProjectViewToExecutionContextEntityMapper` or `ProjectViewToExecutionContextEntityOptionMapper`
 * in your implementation.
 *
 * @param <T> type of yours `ExecutionContext`
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext
 *
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityNullableMapper
 */
interface ExecutionContextConstructor<T : ExecutionContext> {

    fun construct(projectViewTry: Try<ProjectView>): Try<T> =
        projectViewTry.flatMap(::construct)

    fun construct(projectView: ProjectView): Try<T>
}
