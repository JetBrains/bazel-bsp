package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.ProjectViewCliOptions
import org.jetbrains.bsp.bazel.projectview.generator.DefaultProjectViewGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection


import java.nio.file.Path

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
            buildManualTargets = toBuildManualTargetsSection(projectViewCliOptions),
        )

    private fun toJavaPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewJavaPathSection? =
        projectViewCliOptions?.javaPath?.let(::ProjectViewJavaPathSection)

    private fun toBazelPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBazelPathSection? =
        projectViewCliOptions?.bazelPath?.let(::ProjectViewBazelPathSection)

    private fun toTargetsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewTargetsSection? =
        projectViewCliOptions?.targets?.let(::toTargetsSectionNotNull)

    private fun toBuildManualTargetsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBuildManualTargetsSection? =
        projectViewCliOptions?.buildManualTargets?.let(::ProjectViewBuildManualTargetsSection)

    private fun toTargetsSectionNotNull(targets: List<String>?): ProjectViewTargetsSection {
        val includedTargets = calculateIncludedTargets(targets)
        val excludedTargets = calculateExcludedTargets(targets)

        return ProjectViewTargetsSection(includedTargets, excludedTargets)
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
        projectViewCliOptions?.buildFlags?.let { ProjectViewBuildFlagsSection(it) }
}
