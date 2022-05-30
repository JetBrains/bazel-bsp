package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.ProjectViewCliOptions
import org.jetbrains.bsp.bazel.projectview.generator.DefaultProjectViewGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.*
import java.nio.file.Path
import kotlin.io.path.Path

object ProjectViewCLiOptionsProvider {

    private const val EXCLUDED_TARGET_PREFIX = "-"

    fun generateProjectViewAndSave(cliOptions: CliOptions, generatedProjectViewFilePath: Path): Try<ProjectView> {
        val projectView = toProjectView(cliOptions.projectViewCliOptions)

        return DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, generatedProjectViewFilePath)
                .map { projectView }
    }

    private fun toProjectView(projectViewCliOptions: ProjectViewCliOptions?): ProjectView =
            ProjectView(
                    javaPath = toJavaPathSection(projectViewCliOptions),
                    bazelPath = toBazelPathSection(projectViewCliOptions),
                    debuggerAddress = toDebuggerAddressSection(projectViewCliOptions),
                    targets = toTargetsSection(projectViewCliOptions),
                    buildFlags = toBuildFlagsSection(projectViewCliOptions),
                    directories = toDirectoriesSection(projectViewCliOptions),
                    deriveTargetsFlag = toDeriveTargetFlagSection(projectViewCliOptions)
            )

    private fun toJavaPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewJavaPathSection? =
            projectViewCliOptions?.javaPath?.let(::ProjectViewJavaPathSection)

    private fun toBazelPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBazelPathSection? =
            projectViewCliOptions?.bazelPath?.let(::ProjectViewBazelPathSection)

    private fun toTargetsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewTargetsSection? =
            projectViewCliOptions?.targets?.let(::toTargetsSectionNotNull)

    private fun toTargetsSectionNotNull(targets: List<String>): ProjectViewTargetsSection {
        val includedTargets = calculateIncludedValues(targets)
        val excludedTargets = calculateExcludedValues(targets)

        return ProjectViewTargetsSection(includedTargets, excludedTargets)
    }

    private fun toDirectoriesSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDirectoriesSection? =
            projectViewCliOptions?.directories?.let(::toDirectoriesSectionNotNull)

    private fun toDirectoriesSectionNotNull(directories: List<String>): ProjectViewDirectoriesSection {
        val includedDirectories = calculateIncludedDirs(directories)
        val excludedDirectories = calculateExcludedDirs(directories)

        return ProjectViewDirectoriesSection(includedDirectories, excludedDirectories)
    }

    private fun calculateIncludedValues(targets: List<String>): List<BuildTargetIdentifier> =
            targets.filterNot { it.startsWith(EXCLUDED_TARGET_PREFIX) }
                    .map(::BuildTargetIdentifier)

    private fun calculateExcludedValues(targets: List<String>): List<BuildTargetIdentifier> =
            targets.filter { it.startsWith(EXCLUDED_TARGET_PREFIX) }
                    .map { it.drop(1) }
                    .map(::BuildTargetIdentifier)

    private fun calculateIncludedDirs(targets: List<String>): List<Path> =
            targets.filterNot { it.startsWith(EXCLUDED_TARGET_PREFIX) }
                    .map(::Path)

    private fun calculateExcludedDirs(targets: List<String>): List<Path> =
            targets.filter { it.startsWith(EXCLUDED_TARGET_PREFIX) }
                    .map { it.drop(1) }
                    .map(::Path)

    private fun toDebuggerAddressSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDebuggerAddressSection? =
            projectViewCliOptions?.debuggerAddress?.let(::ProjectViewDebuggerAddressSection)

    private fun toBuildFlagsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBuildFlagsSection? =
            projectViewCliOptions?.buildFlags?.let { ProjectViewBuildFlagsSection(it) }

    private fun toDeriveTargetFlagSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDeriveTargetsFlagSection? =
            projectViewCliOptions?.deriveTargetsFlag?.let(::ProjectViewDeriveTargetsFlagSection)
}
