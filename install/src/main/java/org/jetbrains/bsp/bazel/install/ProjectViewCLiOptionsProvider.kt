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
                    deriveTargetsFromDirectories = toDeriveTargetFlagSection(projectViewCliOptions),
                    importDepth = toImportDepthSection(projectViewCliOptions),
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


    private fun toTargetsSectionNotNull(targets: List<String>): ProjectViewTargetsSection {
        val includedTargets = calculateIncludedValues(targets).map(::BuildTargetIdentifier)
        val excludedTargets = calculateExcludedValues(targets).map(::BuildTargetIdentifier)

        return ProjectViewTargetsSection(includedTargets, excludedTargets)
    }

    private fun toDirectoriesSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDirectoriesSection? =
            projectViewCliOptions?.directories?.let(::toDirectoriesSectionNotNull)

    private fun toDirectoriesSectionNotNull(directories: List<String>): ProjectViewDirectoriesSection {
        val includedDirectories = calculateIncludedValues(directories).map(::Path)
        val excludedDirectories = calculateExcludedValues(directories).map(::Path)

        return ProjectViewDirectoriesSection(includedDirectories, excludedDirectories)
    }

    private fun toImportDepthSection(projectViewCliOptions: ProjectViewCliOptions?):  ProjectViewImportDepthSection? =
        projectViewCliOptions?.importDepth?.let(::ProjectViewImportDepthSection)

    private fun calculateIncludedValues(targets: List<String>): List<String> =
            targets.filterNot { it.startsWith(EXCLUDED_TARGET_PREFIX) }

    private fun calculateExcludedValues(targets: List<String>): List<String> =
            targets.filter { it.startsWith(EXCLUDED_TARGET_PREFIX) }
                    .map { it.drop(1) }

    private fun toDebuggerAddressSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDebuggerAddressSection? =
            projectViewCliOptions?.debuggerAddress?.let(::ProjectViewDebuggerAddressSection)

    private fun toBuildFlagsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBuildFlagsSection? =
            projectViewCliOptions?.buildFlags?.let { ProjectViewBuildFlagsSection(it) }

    private fun toDeriveTargetFlagSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDeriveTargetsFromDirectoriesSection? =
            projectViewCliOptions?.deriveTargetsFromDirectories?.let(::ProjectViewDeriveTargetsFromDirectoriesSection)
}
