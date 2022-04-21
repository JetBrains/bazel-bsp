package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.ProjectViewCliOptions
import org.jetbrains.bsp.bazel.projectview.generator.DefaultProjectViewGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.*

class ProjectViewCLiOptionsProvider {
    fun generateProjectViewAndSave(cliOptions: CliOptions): Try<ProjectView> {
        val generator = DefaultProjectViewGenerator()
        val a = generateProjectView(cliOptions)
        return generator.generatePrettyStringAndSaveInFile(a, cliOptions.projectViewFilePath
                ?: cliOptions.workspaceRootDir.resolve("projectview.bazelproject"))
                .map { a }
    }

    private fun generateProjectView(cliOptions: CliOptions): ProjectView {
        val projectViewCliOptions = cliOptions.projectViewCliOptions
        return ProjectView(
                javaPath = javaPathSection(projectViewCliOptions),
                bazelPath = bazelPathSection(projectViewCliOptions),
                debuggerAddress = debuggerAddressSection(projectViewCliOptions),
                targets = targetsSection(projectViewCliOptions),
                buildFlags = buildFlagsSection(projectViewCliOptions),
        )
    }

    private fun javaPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewJavaPathSection? =
            projectViewCliOptions
                    ?.javaPath
                    ?.let { ProjectViewJavaPathSection(it) }

    private fun bazelPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBazelPathSection? =
            projectViewCliOptions
                    ?.bazelPath
                    ?.let { ProjectViewBazelPathSection(it) }

    private fun targetsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewTargetsSection? {
        val excludedTargets = projectViewCliOptions
                ?.targets?.filter { it.startsWith("-") }
                ?.map(::BuildTargetIdentifier)
        val includedTargets = projectViewCliOptions
                ?.targets?.filterNot { it.startsWith("-") }
                ?.map(::BuildTargetIdentifier)
        val excludedTargetsCorrectType = io.vavr.collection.List.ofAll(excludedTargets)
        val includedTargetsCorrectType = io.vavr.collection.List.ofAll(includedTargets)
        return ProjectViewTargetsSection(includedTargetsCorrectType, excludedTargetsCorrectType)
    }

    private fun debuggerAddressSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDebuggerAddressSection? =
            projectViewCliOptions
                    ?.debuggerAddress
                    ?.let { ProjectViewDebuggerAddressSection(it) }

    private fun buildFlagsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBuildFlagsSection? =
            projectViewCliOptions
                    ?.buildFlags
                    ?.let { ProjectViewBuildFlagsSection(io.vavr.collection.List.ofAll(it)) }

}
