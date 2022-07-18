package org.jetbrains.bsp.bazel.server.sync

import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bep.BepOutput
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.net.URI

/** Responsible for querying bazel and constructing Project instance  */
class ProjectResolver(
    private val bazelBspAspectsManager: BazelBspAspectsManager,
    private val workspaceContextProvider: WorkspaceContextProvider,
    private val bazelProjectMapper: BazelProjectMapper,
    private val logger: BspClientLogger,
    private val targetInfoReader: TargetInfoReader
) {
    fun resolve(): Project {
        val workspaceContext = logger.timed(
            "Reading project view adn creating workspace context",
            workspaceContextProvider::currentWorkspaceContext
        )
        val bepOutput = logger.timed<BepOutput>(
            "Building project with aspect"
        ) { buildProjectWithAspect(workspaceContext) }
        val aspectOutputs = logger.timed<Set<URI>>(
            "Reading aspect output paths"
        ) { bepOutput.filesByOutputGroupNameTransitive(BSP_INFO_OUTPUT_GROUP) }
        val rootTargets = bepOutput.rootTargets()
        val targets = logger.timed<Map<String, TargetInfo>>(
            "Parsing aspect outputs"
        ) { targetInfoReader.readTargetMapFromAspectOutputs(aspectOutputs) }
        return logger.timed<Project>(
            "Mapping to internal model"
        ) { bazelProjectMapper.createProject(targets, rootTargets, workspaceContext) }
    }

    private fun buildProjectWithAspect(workspaceContext: WorkspaceContext): BepOutput =
        bazelBspAspectsManager.fetchFilesFromOutputGroups(
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
