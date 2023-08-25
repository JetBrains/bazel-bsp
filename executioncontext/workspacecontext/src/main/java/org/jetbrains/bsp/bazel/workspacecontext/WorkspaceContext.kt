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
    val bazelBinary: BazelBinarySpec,

  /**
     * If true targets with `manual` tag will be built
     *
     * Obtained from `ProjectView` simply by mapping 'build_manual_targets' section.
     */
    val buildManualTargets: BuildManualTargetsSpec,

    // TODO replace `BspInfo`
  /**
     * Path to the `.bazelbsp` dir in the project root
     *
     * Deducted from working directory.
     */
    val dotBazelBspDirPath: DotBazelBspDirPathSpec,

  /**
     * Parameter determining targets importing depth
     *
     * Obtained from `ProjectView` simply by mapping `import_depth` section.
     */
    val importDepth: ImportDepthSpec,
) : ExecutionContext()


object WorkspaceContextConstructor : ExecutionContextConstructor<WorkspaceContext> {

    private val log = LogManager.getLogger(WorkspaceContextConstructor::class.java)

    override fun construct(projectView: ProjectView): Try<WorkspaceContext> {
        log.info("Constructing workspace context for: {}.", projectView)

        // maybe TRY is not that good xd
        return TargetsSpecMapper.map(projectView).flatMap { targetsSpec ->
            BuildFlagsSpecMapper.map(projectView).flatMap { buildFlagsSpec ->
                BazelBinarySpecMapper.map(projectView).flatMap { bazelBinarySpec ->
                    DotBazelBspDirPathSpecMapper.map(projectView).flatMap { dotBazelBspDirPathSpec ->
                        BuildManualTargetsSpecMapper.map(projectView).flatMap { buildManualTargetsSpec ->
                            ImportDepthSpecMapper.map(projectView).map { importDepthSpec ->
                                WorkspaceContext(
                                    targets = targetsSpec,
                                    buildFlags = buildFlagsSpec,
                                    bazelBinary = bazelBinarySpec,
                                    dotBazelBspDirPath = dotBazelBspDirPathSpec,
                                    buildManualTargets = buildManualTargetsSpec,
                                    importDepth = importDepthSpec,
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    override fun constructDefault(): Try<WorkspaceContext> =
        TargetsSpecMapper.default().flatMap { targetsSpec ->
            BuildFlagsSpecMapper.default().flatMap { buildFlagsSpec ->
                BazelBinarySpecMapper.default().flatMap { bazelBinarySpec ->
                    DotBazelBspDirPathSpecMapper.default().flatMap { dotBazelBspDirPathSpec ->
                        BuildManualTargetsSpecMapper.default().flatMap { buildManualTargetsSpec ->
                            ImportDepthSpecMapper.default().map { importDepthSpec ->
                                WorkspaceContext(
                                    targets = targetsSpec,
                                    buildFlags = buildFlagsSpec,
                                    bazelBinary = bazelBinarySpec,
                                    dotBazelBspDirPath = dotBazelBspDirPathSpec,
                                    buildManualTargets = buildManualTargetsSpec,
                                    importDepth = importDepthSpec,
                                )
                            }
                        }
                    }
                }
            }
        }
}
