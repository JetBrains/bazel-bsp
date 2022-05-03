package org.jetbrains.bsp.bazel.workspacecontext

import io.vavr.control.Try
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextConstructor
import org.jetbrains.bsp.bazel.projectview.model.ProjectView

/**
 * Representation of `ExecutionContext` used during server lifetime.
 *
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext
 */
data class WorkspaceContext(
    /**
     * Targets (included and excluded) on which the user wants to work.
     *
     *
     * Obtained from `ProjectView` simply by mapping 'targets' section.
     */
    val targets: ExecutionContextTargetsEntity
) : ExecutionContext()


object WorkspaceContextConstructor : ExecutionContextConstructor<WorkspaceContext> {

    private val log = LogManager.getLogger(WorkspaceContextConstructor::class.java)

    override fun construct(projectView: ProjectView): Try<WorkspaceContext> {
        log.info("Constructing workspace context for: {}.", projectView)

        return WorkspaceContextTargetsEntityMapper.map(projectView).map { WorkspaceContext(it) }
    }
}
