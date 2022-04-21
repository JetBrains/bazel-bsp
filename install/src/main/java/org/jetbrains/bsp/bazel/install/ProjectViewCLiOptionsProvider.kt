package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.ProjectViewCliOptions
import org.jetbrains.bsp.bazel.projectview.generator.DefaultProjectViewGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.*
import java.nio.file.Path

object ProjectViewCLiOptionsProvider {

    private const val EXCLUDED_TARGET_PREFIX = "-"

    fun generateProjectViewAndSave(cliOptions: CliOptions, generatedProjectViewFilePath: Path): Try<ProjectView> {
        val generator = DefaultProjectViewGenerator()
        val projectView = toProjectView(cliOptions.projectViewCliOptions)

        return generator.generatePrettyStringAndSaveInFile(projectView, generatedProjectViewFilePath)
                .map { projectView }
    }

    private fun toProjectView(projectViewCliOptions: ProjectViewCliOptions?): ProjectView =
            ProjectView(
                    javaPath = toJavaPathSection(projectViewCliOptions),
                    bazelPath = toBazelPathSection(projectViewCliOptions),
                    debuggerAddress = toDebuggerAddressSection(projectViewCliOptions),
                    targets = toTargetsSection(projectViewCliOptions),
                    buildFlags = toBuildFlagsSection(projectViewCliOptions),
            )

    private fun toJavaPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewJavaPathSection? =
            projectViewCliOptions?.javaPath?.let(::ProjectViewJavaPathSection)

    private fun toBazelPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBazelPathSection? =
            projectViewCliOptions?.bazelPath?.let(::ProjectViewBazelPathSection)

    private fun toTargetsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewTargetsSection? =
            projectViewCliOptions?.targets?.let(::toTargetsSectionNotNull)

    private fun toTargetsSectionNotNull(targets: List<String>?): ProjectViewTargetsSection {
        val includedTargets = calculateIncludedTargets(targets)
        val excludedTargets = calculateExcludedTargets(targets)

        return ProjectViewTargetsSection(
                io.vavr.collection.List.ofAll(includedTargets),
                io.vavr.collection.List.ofAll(excludedTargets))
    }

    private fun calculateIncludedTargets(targets: List<String>?): List<BuildTargetIdentifier> =
            targets.orEmpty()
                    .filterNot { it.startsWith(EXCLUDED_TARGET_PREFIX) }
                    .map(::BuildTargetIdentifier)

    private fun calculateExcludedTargets(targets: List<String>?): List<BuildTargetIdentifier> =
            targets.orEmpty()
                    .filter { it.startsWith(EXCLUDED_TARGET_PREFIX) }
                    .map { it.drop(1) }
                    .map(::BuildTargetIdentifier)

    private fun toDebuggerAddressSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDebuggerAddressSection? =
            projectViewCliOptions?.debuggerAddress?.let(::ProjectViewDebuggerAddressSection)

    private fun toBuildFlagsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBuildFlagsSection? =
            projectViewCliOptions?.buildFlags?.let { ProjectViewBuildFlagsSection(io.vavr.collection.List.ofAll(it)) }
}
