package org.jetbrains.bsp.bazel.server.sync

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import java.net.URI
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bep.BepOutput
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider

/** Responsible for querying bazel and constructing Project instance  */
class ProjectResolver(
    private val bazelBspAspectsManager: BazelBspAspectsManager,
    private val workspaceContextProvider: WorkspaceContextProvider,
    private val bazelProjectMapper: BazelProjectMapper,
    private val logger: BspClientLogger,
    private val targetInfoReader: TargetInfoReader,
    private val bazelInfo: BazelInfo
) {
    fun resolve(cancelChecker: CancelChecker): Project {

        val workspaceContext = logger.timed(
            "Reading project view and creating workspace context",
            workspaceContextProvider::currentWorkspaceContext
        )
        val bepOutput = logger.timed<BepOutput>(
            "Building project with aspect"
        ) { buildProjectWithAspect(cancelChecker, workspaceContext) }
        val aspectOutputs = logger.timed<Set<URI>>(
            "Reading aspect output paths"
        ) { bepOutput.filesByOutputGroupNameTransitive(BSP_INFO_OUTPUT_GROUP) }
        val rootTargets = when(bazelInfo.release.major){
            // Since bazel 6, the main repository targets are stringified to "@//"-prefixed labels,
            // contrary to "//"-prefixed in older Bazel versions. Unfortunately this does not apply
            // to BEP data, probably due to a bug, so we need to add the "@" prefix here.
            in 0..5 ->  bepOutput.rootTargets()
            else -> bepOutput.rootTargets().map { target -> "@$target" }
        }
        val targets = logger.timed<Map<String, TargetInfo>>(
            "Parsing aspect outputs"
        ) { targetInfoReader.readTargetMapFromAspectOutputs(aspectOutputs) }
        return logger.timed<Project>(
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
        private const val ARTIFACTS_OUTPUT_GROUP = "bsp-ide-resolve-transitive-deps"
    }
}
