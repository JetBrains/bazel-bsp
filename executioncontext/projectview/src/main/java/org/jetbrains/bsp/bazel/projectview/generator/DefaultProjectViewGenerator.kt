package org.jetbrains.bsp.bazel.projectview.generator

import io.vavr.control.Try
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
            ProjectViewDirectoriesSectionGenerator.generatePrettyString(projectView.directories),
            ProjectViewDeriveTargetsFromDirectoriesSectionGenerator.generatePrettyString(projectView.deriveTargetsFromDirectories)
        ).joinToString(separator = "\n\n", postfix = "\n")
}
