package org.jetbrains.bsp.bazel.projectview.generator

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBuildFlagsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewJavaPathSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewTargetsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path

class DefaultProjectViewGenerator : ProjectViewGenerator {

    override fun generatePrettyString(projectView: ProjectView): String =
        listOfNotNull(
            targetsGenerator.generatePrettyString(projectView.targets),
            javaPathSectionGenerator.generatePrettyString(projectView.javaPath),
            buildFlagsGenerator.generatePrettyString(projectView.buildFlags),
        ).joinToString(separator = "\n\n", postfix = "\n")

    override fun generatePrettyStringAndSaveInFile(projectView: ProjectView, filePath: Path): Try<Void> {
        TODO("Not yet implemented")
    }

    private companion object {
        private val targetsGenerator = ProjectViewTargetsSectionGenerator()
        private val javaPathSectionGenerator = ProjectViewJavaPathSectionGenerator()
        private val buildFlagsGenerator = ProjectViewBuildFlagsSectionGenerator()
    }
}
