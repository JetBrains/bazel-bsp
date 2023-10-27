package org.jetbrains.bsp.bazel.server.sync

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bep.BepOutput
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspFallbackAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManagerResult
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider

/** Responsible for querying bazel and constructing Project instance  */
class ProjectResolver(
        private val bazelBspAspectsManager: BazelBspAspectsManager,
        private val bazelBspFallbackAspectsManager: BazelBspFallbackAspectsManager,
        private val workspaceContextProvider: WorkspaceContextProvider,
        private val bazelProjectMapper: BazelProjectMapper,
        private val bspLogger: BspClientLogger,
        private val targetInfoReader: TargetInfoReader,
        private val bazelInfo: BazelInfo,
        private val metricsLogger: MetricsLogger?
) {
    private fun <T> measured(description: String, f: () -> T): T {
        return Measurements.measure(f, description, metricsLogger, bspLogger)
    }

    fun resolve(cancelChecker: CancelChecker): Project {

        val workspaceContext = measured(
            "Reading project view and creating workspace context",
            workspaceContextProvider::currentWorkspaceContext
        )
        val buildAspectResult = measured(
            "Building project with aspect"
        ) { buildProjectWithAspect(cancelChecker, workspaceContext) }
        val allTargetNames =
            if (buildAspectResult.isFailure)
                measured(
                    "Fetching all possible target names"
                ) { bazelBspFallbackAspectsManager.getAllPossibleTargets(cancelChecker).let { formatTargetsIfNeeded(it) } }
            else
                emptyList()
        val aspectOutputs = measured(
            "Reading aspect output paths"
        ) { buildAspectResult.bepOutput.filesByOutputGroupNameTransitive(BSP_INFO_OUTPUT_GROUP) }
        val rootTargets = buildAspectResult.bepOutput.rootTargets().let { formatTargetsIfNeeded(it) }
        val targets = measured(
            "Parsing aspect outputs"
        ) { targetInfoReader.readTargetMapFromAspectOutputs(aspectOutputs) }
        return measured(
            "Mapping to internal model"
        ) { bazelProjectMapper.createProject(targets, rootTargets.toSet(), allTargetNames, workspaceContext) }
    }

    private fun buildProjectWithAspect(cancelChecker: CancelChecker, workspaceContext: WorkspaceContext): BazelBspAspectsManagerResult =
        bazelBspAspectsManager.fetchFilesFromOutputGroups(
            cancelChecker,
            workspaceContext.targets,
            ASPECT_NAME,
            listOf(BSP_INFO_OUTPUT_GROUP, ARTIFACTS_OUTPUT_GROUP)
        )

    private fun formatTargetsIfNeeded(targets: Collection<String>): List<String> =
        when(bazelInfo.release.major){
            // Since bazel 6, the main repository targets are stringified to "@//"-prefixed labels,
            // contrary to "//"-prefixed in older Bazel versions. Unfortunately this does not apply
            // to BEP data, probably due to a bug, so we need to add the "@" prefix here.
            in 0..5 ->  targets.toList()
            else -> targets.map { "@$it" }
        }.toList()

    companion object {
        private const val ASPECT_NAME = "bsp_target_info_aspect"
        private const val BSP_INFO_OUTPUT_GROUP = "bsp-target-info-transitive-deps"
        private const val ARTIFACTS_OUTPUT_GROUP = "external-deps-resolve-transitive-deps"
    }
}
