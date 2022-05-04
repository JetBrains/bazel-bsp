package org.jetbrains.bsp.bazel.executioncontext.api

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.ProjectView

/**
 * Base `ExecutionContext` entity class - you need to extend it or
 * `ExecutionContextListEntity` or `ExecutionContextSingletonEntity` if you want to create your entity.
 *
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextListEntity
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
 */
abstract class ExecutionContextEntity


/**
 * Base list-based `ExecutionContext` entity class - you need to extend it if you want to
 * create your list-based entity.
 */
abstract class ExecutionContextListEntity<T> : ExecutionContextEntity() {
    abstract val includedValues: List<T>
    abstract val excludedValues: List<T>
}


/**
 * Base single-value `ExecutionContext` entity class - you need to extend it if you want
 * to create your single-value entity.
 */
abstract class ExecutionContextSingletonEntity<T> : ExecutionContextEntity() {
    abstract val value: T
}


/** `ProjectViewToExecutionContextEntityMapper` mapping failed? Return ('throw') it. */
class ProjectViewToExecutionContextEntityMapperException(entityName: String, message: String) :
    Exception("Mapping project view into '$entityName' failed! $message")


interface ProjectViewToExecutionContextEntityBaseMapper<T> {
    fun map(projectView: ProjectView): Try<T>
}


/**
 * Maps `ProjectView` into `ExecutionContextEntity`.
 * It takes entire `ProjectView` because sometimes in order to create one `ExecutionContextEntity`
 * you may want to use multiple `ProjectView` sections.
 *
 * @param <T> type of the mapped entity
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityBaseMapper
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntity
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
interface ProjectViewToExecutionContextEntityMapper<T : ExecutionContextEntity> :
    ProjectViewToExecutionContextEntityBaseMapper<T>


/**
 * Maps `ProjectView` into `ExecutionContextEntity?`.
 * It takes entire `ProjectView` because sometimes in order to create one
 * `ExecutionContextEntity?` you may want to use multiple
 * `ProjectView` sections and the result might be empty.
 *
 * @param <T> type of the mapped entity
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityBaseMapper
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntity
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
interface ProjectViewToExecutionContextEntityNullableMapper<T : ExecutionContextEntity> :
    ProjectViewToExecutionContextEntityBaseMapper<T?>
