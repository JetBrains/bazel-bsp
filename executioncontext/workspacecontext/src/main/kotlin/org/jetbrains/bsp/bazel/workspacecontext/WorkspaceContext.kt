package org.jetbrains.bsp.bazel.workspacecontext

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextConstructor
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path

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
     * Obtained from `ProjectView` simply by mapping 'targets' section
     * or derived from 'directories' if 'derive_targets_from_directories' is true.
     */
    val targets: TargetsSpec,

    /**
     * Directories (included and excluded) in the project.
     *
     * Obtained from 'ProjectView' simply by mapping 'directories' section if not null,
     * otherwise the whole project is included (project root is included).
     */
    val directories: DirectoriesSpec,

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
    /**
     * Parameter determining which rules should be used by Bazel BSP, if empty Bazel is queried.
     *
     * Obtained from `ProjectView` simply by mapping `import_depth` section.
     */
    val enabledRules: EnabledRulesSpec,
) : ExecutionContext()

class WorkspaceContextConstructor(workspaceRoot: Path) : ExecutionContextConstructor<WorkspaceContext> {

    private val dotBazelBspDirPathSpecExtractor = DotBazelBspDirPathSpecExtractor(workspaceRoot)
    private val directoriesSpecExtractor = DirectoriesSpecExtractor(workspaceRoot)

    private val log = LogManager.getLogger(WorkspaceContextConstructor::class.java)

    override fun construct(projectView: ProjectView): WorkspaceContext {
        log.info("Constructing workspace context for: {}.", projectView)

        return WorkspaceContext(
            targets = TargetsSpecExtractor.fromProjectView(projectView),
            directories = directoriesSpecExtractor.fromProjectView(projectView),
            buildFlags = BuildFlagsSpecExtractor.fromProjectView(projectView),
            bazelBinary = BazelBinarySpecExtractor.fromProjectView(projectView),
            dotBazelBspDirPath = dotBazelBspDirPathSpecExtractor.fromProjectView(projectView),
            buildManualTargets = BuildManualTargetsSpecExtractor.fromProjectView(projectView),
            importDepth = ImportDepthSpecExtractor.fromProjectView(projectView),
            enabledRules = EnabledRulesSpecExtractor.fromProjectView(projectView),
        )
    }
}
