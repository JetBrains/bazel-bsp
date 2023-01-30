package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.ProjectViewCliOptions
import org.jetbrains.bsp.bazel.projectview.generator.DefaultProjectViewGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDeriveTargetsFromDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import java.nio.file.Path
import kotlin.io.path.Path

object ProjectViewCLiOptionsProvider {

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
            deriveTargetsFromDirectories = toDeriveTargetFlagSection(projectViewCliOptions),
            importDepth = toImportDepthSection(projectViewCliOptions),
            buildManualTargets = toBuildManualTargetsSection(projectViewCliOptions),
        )

    private fun toJavaPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewJavaPathSection? =
        projectViewCliOptions?.javaPath?.let(::ProjectViewJavaPathSection)

    private fun toBazelPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBazelPathSection? =
        projectViewCliOptions?.bazelPath?.let(::ProjectViewBazelPathSection)

    private fun toTargetsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewTargetsSection? =
        when {
            projectViewCliOptions == null -> null
            projectViewCliOptions.targets != null || projectViewCliOptions.excludedTargets != null ->
                toTargetsSectionNotNull(projectViewCliOptions)

            else -> null
        }

    private fun toTargetsSectionNotNull(projectViewCliOptions: ProjectViewCliOptions): ProjectViewTargetsSection {
        val includedTargets = projectViewCliOptions.targets.orEmpty().map { BuildTargetIdentifier(it) }
        val excludedTargets = projectViewCliOptions.excludedTargets.orEmpty().map { BuildTargetIdentifier(it) }

        return ProjectViewTargetsSection(includedTargets, excludedTargets)
    }

    private fun toBuildManualTargetsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBuildManualTargetsSection? =
        projectViewCliOptions?.buildManualTargets?.let(::ProjectViewBuildManualTargetsSection)


    private fun toDirectoriesSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDirectoriesSection? =
        when {
            projectViewCliOptions == null -> null
            projectViewCliOptions.directories != null || projectViewCliOptions.excludedDirectories != null ->
                toDirectoriesSectionNotNull(projectViewCliOptions)

            else -> null
        }

    private fun toDirectoriesSectionNotNull(projectViewCliOptions: ProjectViewCliOptions): ProjectViewDirectoriesSection {
        val includedDirectories = projectViewCliOptions.directories.orEmpty().map { Path(it) }
        val excludedDirectories = projectViewCliOptions.excludedDirectories.orEmpty().map { Path(it) }

        return ProjectViewDirectoriesSection(includedDirectories, excludedDirectories)
    }

    private fun toImportDepthSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewImportDepthSection? =
        projectViewCliOptions?.importDepth?.let(::ProjectViewImportDepthSection)

    private fun toDebuggerAddressSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDebuggerAddressSection? =
        projectViewCliOptions?.debuggerAddress?.let(::ProjectViewDebuggerAddressSection)

    private fun toBuildFlagsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBuildFlagsSection? =
        projectViewCliOptions?.buildFlags?.let { ProjectViewBuildFlagsSection(it) }

    private fun toDeriveTargetFlagSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDeriveTargetsFromDirectoriesSection? =
        projectViewCliOptions?.deriveTargetsFromDirectories?.let(::ProjectViewDeriveTargetsFromDirectoriesSection)
}
