package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import java.io.File
import java.nio.file.Path

data class BazelBinarySpec(
    override val value: Path
) : ExecutionContextSingletonEntity<Path>()

internal object BazelBinarySpecMapper : ProjectViewToExecutionContextEntityMapper<BazelBinarySpec> {

    override fun map(projectView: ProjectView): Result<BazelBinarySpec> =
        when (projectView.bazelBinary) {
            null -> findBazelOnPath()
            else -> Result.success(map(projectView.bazelBinary!!))
        }

    override fun default(): Result<BazelBinarySpec> = findBazelOnPath()

    private fun findBazelOnPath(): Result<BazelBinarySpec> =
        findBazelOnPathOrNull()?.let { Result.success(it) }
            ?: Result.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    "bazel path",
                    "Could not find bazel on your PATH"
                )
            )

    private fun findBazelOnPathOrNull(): BazelBinarySpec? =
        splitPath()
            .map { mapToBazel(it) }
            .firstOrNull { it.canExecute() }
            ?.toPath()
            ?.let { BazelBinarySpec(it) }

    private fun splitPath(): List<String> = System.getenv("PATH").split(File.pathSeparator)

    private fun mapToBazel(path: String): File = File(path, calculateBazeliskExecName())

    // TODO: update tests for the whole flow and mock different OSes
    private fun calculateBazeliskExecName(): String {
        val osName = System.getProperty("os.name").lowercase()
        return with(osName) {
            when {
                startsWith("windows") -> "bazel.exe"
                else -> "bazel"
            }
        }
    }

    private fun map(bazelBinarySection: ProjectViewBazelBinarySection): BazelBinarySpec =
        BazelBinarySpec(bazelBinarySection.value)

}
