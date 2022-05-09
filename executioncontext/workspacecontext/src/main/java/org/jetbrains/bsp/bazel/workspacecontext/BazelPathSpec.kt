package org.jetbrains.bsp.bazel.workspacecontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import java.io.File
import java.nio.file.Path

data class BazelPathSpec(
    override val value: Path
) : ExecutionContextSingletonEntity<Path>()

internal object BazelPathSpecMapper : ProjectViewToExecutionContextEntityMapper<BazelPathSpec> {

    override fun map(projectView: ProjectView): Try<BazelPathSpec> =
        when (projectView.bazelPath) {
            null -> findBazelOnPath()
            else -> Try.success(map(projectView.bazelPath!!))
        }

    private fun findBazelOnPath(): Try<BazelPathSpec> =
        findBazelOnPathOrNull()?.let { Try.success(it) }
            ?: Try.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    "bazel path",
                    "Could not find bazel on your PATH"
                )
            )

    private fun findBazelOnPathOrNull(): BazelPathSpec? =
        splitPath()
            .filterNot { isBazeliskPath(it) }
            .map { mapToBazel(it) }
            .firstOrNull { it.canExecute() }
            ?.toPath()
            ?.let { BazelPathSpec(it) }

    private fun splitPath(): List<String> = System.getenv("PATH").split(File.pathSeparator)

    private fun isBazeliskPath(path: String): Boolean = path.contains("bazelisk/")

    private fun mapToBazel(path: String): File = File(path, "bazel")

    private fun map(bazelPathSection: ProjectViewBazelPathSection): BazelPathSpec =
        BazelPathSpec(bazelPathSection.value)
}
