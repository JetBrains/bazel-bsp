package org.jetbrains.bsp.bazel.projectview.generator

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBazelPathSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewBuildFlagsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewDebuggerAddressSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewJavaPathSectionGenerator
import org.jetbrains.bsp.bazel.projectview.generator.sections.ProjectViewTargetsSectionGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.utils.dope.DopeFiles
import java.nio.file.Path

class DefaultProjectViewGenerator : ProjectViewGenerator {

    override fun generatePrettyStringAndSaveInFile(projectView: ProjectView, filePath: Path): Try<Void> =
        DopeFiles.writeText(filePath, generatePrettyString(projectView))

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
