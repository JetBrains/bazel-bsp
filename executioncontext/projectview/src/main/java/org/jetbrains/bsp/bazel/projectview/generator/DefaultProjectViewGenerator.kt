package org.jetbrains.bsp.bazel.projectview.generator

import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBazelBinarySectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBuildFlagsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBuildManualTargetsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewDeriveTargetsFromDirectoriesSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewDirectoriesSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewImportDepthSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewTargetsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Files
import java.nio.file.Path

object DefaultProjectViewGenerator : ProjectViewGenerator {

    override fun generatePrettyStringAndSaveInFile(projectView: ProjectView, filePath: Path) {
        Files.createDirectories(filePath.parent)
        Files.writeString(filePath, generatePrettyString(projectView))
    }

    override fun generatePrettyString(projectView: ProjectView): String =
        listOfNotNull(
            ProjectViewTargetsSectionGenerator.generatePrettyString(projectView.targets),
            ProjectViewBazelBinarySectionGenerator.generatePrettyString(projectView.bazelBinary),
            ProjectViewBuildFlagsSectionGenerator.generatePrettyString(projectView.buildFlags),
            ProjectViewBuildManualTargetsSectionGenerator.generatePrettyString(projectView.buildManualTargets),
            ProjectViewDirectoriesSectionGenerator.generatePrettyString(projectView.directories),
            ProjectViewDeriveTargetsFromDirectoriesSectionGenerator.generatePrettyString(projectView.deriveTargetsFromDirectories),
            ProjectViewImportDepthSectionGenerator.generatePrettyString(projectView.importDepth),
        ).joinToString(separator = "\n\n", postfix = "\n")
}
