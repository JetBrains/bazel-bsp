package org.jetbrains.bsp.bazel.executioncontext.api

import org.jetbrains.bsp.bazel.projectview.model.ProjectView

/**
 * Base `ExecutionContext` entity class - you need to extend it or
 * `ExecutionContextListEntity` or `ExecutionContextSingletonEntity` if you want to create your entity.
 *
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextExcludableListEntity
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
 */
abstract class ExecutionContextEntity

/**
 * Base list-based `ExecutionContext` entity class - you need to extend it if you want to
 * create your list-based entity.
 */
abstract class ExecutionContextListEntity<T> : ExecutionContextEntity() {
    abstract val values: List<T>
}

/**
 * Base list-based `ExecutionContext` entity class - you need to extend it if you want to
 * create your list-based with excluded values entity.
 */
abstract class ExecutionContextExcludableListEntity<T> : ExecutionContextListEntity<T>() {
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
class ExecutionContextEntityExtractorException(entityName: String, message: String) :
    Exception("Mapping project view into '$entityName' failed! $message")


/**
 * Maps `ProjectView` into `ExecutionContextEntity`.
 * It takes entire `ProjectView` because sometimes in order to create one `ExecutionContextEntity`
 * you may want to use multiple `ProjectView` sections.
 *
 * @param <T> type of the mapped entity
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntity
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
interface ExecutionContextEntityExtractor<T> {
    fun fromProjectView(projectView: ProjectView): T
}
