package org.jetbrains.bsp.bazel.server.sync

import com.google.protobuf.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bep.BepOutput
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/** Responsible for querying bazel and constructing Project instance  */
class ProjectResolver(
    private val bazelBspAspectsManager: BazelBspAspectsManager,
    private val workspaceContextProvider: WorkspaceContextProvider,
    private val bazelProjectMapper: BazelProjectMapper,
    private val logger: BspClientLogger
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
        ) { readTargetMapFromAspectOutputs(aspectOutputs) }
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

    private fun readTargetMapFromAspectOutputs(files: Set<URI>): Map<String, TargetInfo> =
        runBlocking(Dispatchers.IO) {
            files.asFlow().map(::readTargetInfoFromFile).toList().associateBy(TargetInfo::getId)
        }

    private fun readTargetInfoFromFile(uri: URI): TargetInfo {
        val builder = TargetInfo.newBuilder()
        val parser = TextFormat.Parser.newBuilder().setAllowUnknownFields(true).build()
        parser.merge(Files.newBufferedReader(Paths.get(uri), StandardCharsets.UTF_8), builder)
        return builder.build()
    }

    companion object {
        private const val ASPECT_NAME = "bsp_target_info_aspect"
        private const val BSP_INFO_OUTPUT_GROUP = "bsp-target-info-transitive-deps"
        private const val ARTIFACTS_OUTPUT_GROUP = "bsp-ide-resolve-transitive-deps"
    }
}
