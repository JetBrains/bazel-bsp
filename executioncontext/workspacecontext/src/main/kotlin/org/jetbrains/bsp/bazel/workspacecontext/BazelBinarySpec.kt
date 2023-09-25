package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractorException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import java.io.File
import java.nio.file.Path

data class BazelBinarySpec(
    override val value: Path
) : ExecutionContextSingletonEntity<Path>()

internal object BazelBinarySpecExtractor : ExecutionContextEntityExtractor<BazelBinarySpec> {

    override fun fromProjectView(projectView: ProjectView): BazelBinarySpec =
        when (projectView.bazelBinary) {
            null -> findBazelOnPath()
            else -> map(projectView.bazelBinary!!)
        }

    private fun findBazelOnPath(): BazelBinarySpec =
        findBazelOnPathOrNull()
            ?: throw
                ExecutionContextEntityExtractorException(
                    "bazel path",
                    "Could not find bazel on your PATH"
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
