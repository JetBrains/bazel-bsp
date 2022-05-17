package org.jetbrains.bsp.bazel.workspacecontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import java.io.File
import java.nio.file.Path

data class BuildManualTargetsSpec(override val value: Boolean) : ExecutionContextSingletonEntity<Boolean>()

internal object BuildManualTargetsSpecMapper : ProjectViewToExecutionContextEntityMapper<BuildManualTargetsSpec> {
    override fun map(projectView: ProjectView): Try<BuildManualTargetsSpec> {
        when (projectView.buildManualTargets) {
            null ->
        }
    }

    override fun default(): Try<BuildManualTargetsSpec> {
        TODO("Not yet implemented")
    }

    private fun findManualTargetsOnPath(): Try<BazelPathSpec> =
            BuildManualTargetsSpecMapper.findManualTargetsOnPathOrNull()?.let { Try.success(it) }
                    ?: Try.failure(
                            ProjectViewToExecutionContextEntityMapperException(
                                    "build manual targets",
                                    "Could not find manual targets on your PATH"
                            )
                    )

    private fun findManualTargetsOnPathOrNull(): BazelPathSpec? =
            BuildManualTargetsSpecMapper.splitPath()
                    .filterNot { BuildManualTargetsSpecMapper.isBazeliskPath(it) }
                    .map { BazelPathSpecMapper.mapToBazel(it) }
                    .firstOrNull { it.canExecute() }
                    ?.toPath()
                    ?.let { BuildManualTargetsSpec(it) }

    private fun splitPath(): List<String> = System.getenv("MANUAL").split(File.pathSeparator)

    private fun isBazeliskPath(boolean: String): Boolean = boolean.contains("true")

    private fun mapToManualTargets(boolean: String): File = File(boolean, "bazel")

    private fun map(buildManualTargetsSection: ProjectViewBuildManualTargetsSection): BuildManualTargetsSpec =
            BuildManualTargetsSpec(buildManualTargetsSection.value)
}
