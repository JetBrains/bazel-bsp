package org.jetbrains.bsp.bazel.projectview.generator

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBazelPathSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBuildFlagsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewDebuggerAddressSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewJavaPathSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewTargetsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Files
import java.nio.file.Path

class DefaultProjectViewGenerator : ProjectViewGenerator {

    override fun generatePrettyStringAndSaveInFile(projectView: ProjectView, filePath: Path): Try<Void> =
        writeStringToFile(filePath, generatePrettyString(projectView))

    private fun writeStringToFile(destinationPath: Path, string: String): Try<Void> {
        return Try.run { Files.writeString(destinationPath, string) }
    }

    override fun generatePrettyString(projectView: ProjectView): String =
        listOfNotNull(
            targetsGenerator.generatePrettyString(projectView.targets),
            bazelPathGenerator.generatePrettyString(projectView.bazelPath),
            debuggerAddressGenerator.generatePrettyString(projectView.debuggerAddress),
            javaPathSectionGenerator.generatePrettyString(projectView.javaPath),
            buildFlagsGenerator.generatePrettyString(projectView.buildFlags),
        ).joinToString(separator = "\n\n", postfix = "\n")

    private companion object {
        private val targetsGenerator = ProjectViewTargetsSectionGenerator()
        private val debuggerAddressGenerator = ProjectViewDebuggerAddressSectionGenerator()
        private val bazelPathGenerator = ProjectViewBazelPathSectionGenerator()
        private val javaPathSectionGenerator = ProjectViewJavaPathSectionGenerator()
        private val buildFlagsGenerator = ProjectViewBuildFlagsSectionGenerator()
    }
}
