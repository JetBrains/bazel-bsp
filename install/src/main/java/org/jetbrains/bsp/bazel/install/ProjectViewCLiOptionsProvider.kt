package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.projectview.generator.DefaultProjectViewGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.*

class ProjectViewCLiOptionsProvider {

    fun generateProjectViewAndSave(cliOptions: CliOptions): Try<ProjectView> {
        return generateAndSave(cliOptions)
    }

    private fun generateAndSave(cliOptions: CliOptions): Try<ProjectView> {
        val generator = DefaultProjectViewGenerator()
        val a = generateProjectView(cliOptions)
        return generator.generatePrettyStringAndSaveInFile(a, cliOptions.projectViewFilePath
                ?: cliOptions.workspaceRootDir.resolve("projectview.bazelproject"))
                .map { a }
    }

    private fun generateProjectView(cliOptions: CliOptions): ProjectView {
        return ProjectView(
                javaPath = javaPathSection(cliOptions),
                bazelPath = bazelPathSection(cliOptions),
                debuggerAddress = debuggerAddressSection(cliOptions),
                targets = targetsSection(cliOptions),
                buildFlags = buildFlagsSection(cliOptions),
        )
    }

    private fun javaPathSection(cliOptions: CliOptions): ProjectViewJavaPathSection? =
            cliOptions.projectViewCliOptions
                    ?.javaPath
                    ?.let { ProjectViewJavaPathSection(it) }

    private fun bazelPathSection(cliOptions: CliOptions): ProjectViewBazelPathSection? =
            cliOptions.projectViewCliOptions
                    ?.bazelPath
                    ?.let { ProjectViewBazelPathSection(it) }

    private fun targetsSection(cliOptions: CliOptions): ProjectViewTargetsSection? {
        val excludedTargets = cliOptions.projectViewCliOptions
                ?.targets?.filter { it.startsWith("-") }
                ?.map(::BuildTargetIdentifier)
        val includedTargets = cliOptions.projectViewCliOptions
                ?.targets?.filterNot { it.startsWith("-") }
                ?.map(::BuildTargetIdentifier)
        val excludedTargetsCorrectType = io.vavr.collection.List.ofAll(excludedTargets)
        val includedTargetsCorrectType = io.vavr.collection.List.ofAll(includedTargets)
        return ProjectViewTargetsSection(includedTargetsCorrectType, excludedTargetsCorrectType)
    }

    private fun debuggerAddressSection(cliOptions: CliOptions): ProjectViewDebuggerAddressSection? =
            cliOptions.projectViewCliOptions
                    ?.debuggerAddress
                    ?.let { ProjectViewDebuggerAddressSection(it) }

    private fun buildFlagsSection(cliOptions: CliOptions): ProjectViewBuildFlagsSection? =
            cliOptions.projectViewCliOptions
                    ?.buildFlags
                    ?.let { ProjectViewBuildFlagsSection(io.vavr.collection.List.ofAll(it)) }

}
