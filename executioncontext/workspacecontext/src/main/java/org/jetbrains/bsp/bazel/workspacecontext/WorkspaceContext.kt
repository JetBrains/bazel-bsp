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
    val targets: TargetsSpec,

    /**
     * Build flags which should be added to each bazel call.
     *
     * Obtained from `ProjectView` simply by mapping `build_flags` section.
     */
    val buildFlags: BuildFlagsSpec,

    /**
     * Path to bazel which should be used in the bazel runner.
     *
     * Obtained from `ProjectView` if not null, otherwise deducted from `PATH`.
     */
    val bazelPath: BazelPathSpec,
) : ExecutionContext()


object WorkspaceContextConstructor : ExecutionContextConstructor<WorkspaceContext> {

    private val log = LogManager.getLogger(WorkspaceContextConstructor::class.java)

    override fun construct(projectView: ProjectView): Try<WorkspaceContext> {
        log.info("Constructing workspace context for: {}.", projectView)

        // maybe TRY is not that good xd
        return TargetsSpecMapper.map(projectView).flatMap { targetsSpec ->
            BuildFlagsSpecMapper.map(projectView).flatMap { buildFlagsSpec ->
                BazelPathSpecMapper.map(projectView).map { bazelPathSpec ->
                    WorkspaceContext(
                        targets = targetsSpec,
                        buildFlags = buildFlagsSpec,
                        bazelPath = bazelPathSpec,
                    )
                }
            }
        }
    }

    override fun constructDefault(): Try<WorkspaceContext> =
        TargetsSpecMapper.default().flatMap { targetsSpec ->
            BuildFlagsSpecMapper.default().flatMap { buildFlagsSpec ->
                BazelPathSpecMapper.default().map { bazelPathSpec ->
                    WorkspaceContext(
                        targets = targetsSpec,
                        buildFlags = buildFlagsSpec,
                        bazelPath = bazelPathSpec,
                    )
                }
            }
        }
}
