package org.jetbrains.bsp.bazel.server.sync

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.commons.Stopwatch
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bep.BepOutput
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.net.URI
import java.util.Locale

/** Responsible for querying bazel and constructing Project instance  */
class ProjectResolver(
        private val bazelBspAspectsManager: BazelBspAspectsManager,
        private val workspaceContextProvider: WorkspaceContextProvider,
        private val bazelProjectMapper: BazelProjectMapper,
        private val bspLogger: BspClientLogger,
        private val targetInfoReader: TargetInfoReader,
        private val bazelInfo: BazelInfo,
        private val metricsLogger: MetricsLogger?
) {
    private fun <T> measured(description: String, f: () -> T): T {
        val sw = Stopwatch.start()
        val result = f()
        val duration = sw.stop()
        val taskKey = description.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), ".")
        metricsLogger?.logMemory("$taskKey.memory.mb")
        metricsLogger?.log("$taskKey.time.ms", duration.toMillis())
        bspLogger.logDuration(description, duration)
        return result
    }

    fun resolve(cancelChecker: CancelChecker): Project {

        val workspaceContext = measured(
            "Reading project view and creating workspace context",
            workspaceContextProvider::currentWorkspaceContext
        )
        val bepOutput = measured(
            "Building project with aspect"
        ) { buildProjectWithAspect(cancelChecker, workspaceContext) }
        val aspectOutputs = measured<Set<URI>>(
            "Reading aspect output paths"
        ) { bepOutput.filesByOutputGroupNameTransitive(BSP_INFO_OUTPUT_GROUP) }
        val rootTargets = when(bazelInfo.release.major){
            // Since bazel 6, the main repository targets are stringified to "@//"-prefixed labels,
            // contrary to "//"-prefixed in older Bazel versions. Unfortunately this does not apply
            // to BEP data, probably due to a bug, so we need to add the "@" prefix here.
            in 0..5 ->  bepOutput.rootTargets()
            else -> bepOutput.rootTargets().map { "@$it" }
        }
        val targets = measured(
            "Parsing aspect outputs"
        ) { targetInfoReader.readTargetMapFromAspectOutputs(aspectOutputs) }
        return measured(
            "Mapping to internal model"
        ) { bazelProjectMapper.createProject(targets, rootTargets.toSet(), workspaceContext) }
    }

    private fun buildProjectWithAspect(cancelChecker: CancelChecker, workspaceContext: WorkspaceContext): BepOutput =
        bazelBspAspectsManager.fetchFilesFromOutputGroups(
            cancelChecker,
            workspaceContext.targets,
            ASPECT_NAME,
            listOf(BSP_INFO_OUTPUT_GROUP, ARTIFACTS_OUTPUT_GROUP)
        )


    companion object {
        private const val ASPECT_NAME = "bsp_target_info_aspect"
        private const val BSP_INFO_OUTPUT_GROUP = "bsp-target-info-transitive-deps"
        private const val ARTIFACTS_OUTPUT_GROUP = "external-deps-resolve-transitive-deps"
    }
}
