package org.jetbrains.bsp.bazel.projectview.generator

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewTargetsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBazelPathSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewDebuggerAddressSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewJavaPathSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBuildFlagsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBuildManualTargetsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.*
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.utils.dope.DopeFiles
import java.nio.file.Path

object DefaultProjectViewGenerator : ProjectViewGenerator {

    override fun generatePrettyStringAndSaveInFile(projectView: ProjectView, filePath: Path): Try<Void> =
        DopeFiles.writeText(filePath, generatePrettyString(projectView))

    override fun generatePrettyString(projectView: ProjectView): String =
        listOfNotNull(
            ProjectViewTargetsSectionGenerator.generatePrettyString(projectView.targets),
            ProjectViewBazelPathSectionGenerator.generatePrettyString(projectView.bazelPath),
            ProjectViewDebuggerAddressSectionGenerator.generatePrettyString(projectView.debuggerAddress),
            ProjectViewJavaPathSectionGenerator.generatePrettyString(projectView.javaPath),
            ProjectViewBuildFlagsSectionGenerator.generatePrettyString(projectView.buildFlags),
            ProjectViewBuildManualTargetsSectionGenerator.generatePrettyString(projectView.buildManualTargets),
            ProjectViewDirectoriesSectionGenerator.generatePrettyString(projectView.directories),
            ProjectViewDeriveTargetsFromDirectoriesSectionGenerator.generatePrettyString(projectView.deriveTargetsFromDirectories),
            ProjectViewImportDepthSectionGenerator.generatePrettyString(projectView.importDepth),
            ProjectViewProduceTraceLogSectionGenerator.generatePrettyString(projectView.produceTraceLog),
        ).joinToString(separator = "\n\n", postfix = "\n")
}
