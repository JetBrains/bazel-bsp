package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.collection.List
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.ProjectViewCliOptions
import org.jetbrains.bsp.bazel.projectview.generator.DefaultProjectViewGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.*

class ProjectViewCLiOptionsProvider {

    fun generateProjectViewAndSave(cliOptions: CliOptions): Try<ProjectView> {
        val generator = DefaultProjectViewGenerator()
        val generatedProjectView = generateProjectView(cliOptions)
        return generator.generatePrettyStringAndSaveInFile(generatedProjectView, cliOptions.projectViewFilePath
                ?: cliOptions.workspaceRootDir.resolve("projectview.bazelproject"))
                .map { generatedProjectView }
    }

    private fun generateProjectView(cliOptions: CliOptions): ProjectView {
        return ProjectView(
                javaPath = javaPathSection(cliOptions.projectViewCliOptions),
                bazelPath = bazelPathSection(cliOptions.projectViewCliOptions),
                debuggerAddress = debuggerAddressSection(cliOptions.projectViewCliOptions),
                targets = targetsSection(cliOptions.projectViewCliOptions),
                buildFlags = buildFlagsSection(cliOptions.projectViewCliOptions),
        )
    }

    private fun javaPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewJavaPathSection? =
            projectViewCliOptions
                    ?.javaPath
                    ?.let(::ProjectViewJavaPathSection)

    private fun bazelPathSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBazelPathSection? =
            projectViewCliOptions
                    ?.bazelPath
                    ?.let(::ProjectViewBazelPathSection)

    private fun targetsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewTargetsSection? {

        val excludedTargets = excludedTargetProvider(projectViewCliOptions)
        val includedTargets = includedTargetsProvider(projectViewCliOptions)
        return ProjectViewTargetsSection(includedTargets, excludedTargets)
    }

    private fun excludedTargetProvider(projectViewCliOptions: ProjectViewCliOptions?): List<BuildTargetIdentifier> {
        val excludedTargets = projectViewCliOptions
                ?.targets?.filter { it.startsWith("-") }
                ?.map(::BuildTargetIdentifier)
        return List.ofAll(excludedTargets)
    }

    private fun includedTargetsProvider(projectViewCliOptions: ProjectViewCliOptions?): List<BuildTargetIdentifier> {
        val includedTargets = projectViewCliOptions
                ?.targets?.filterNot { it.startsWith("-") }
                ?.map(::BuildTargetIdentifier)
        return List.ofAll(includedTargets)
    }

    private fun debuggerAddressSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewDebuggerAddressSection? =
            projectViewCliOptions
                    ?.debuggerAddress
                    ?.let(::ProjectViewDebuggerAddressSection)

    private fun buildFlagsSection(projectViewCliOptions: ProjectViewCliOptions?): ProjectViewBuildFlagsSection? =
            projectViewCliOptions
                    ?.buildFlags
                    ?.let { ProjectViewBuildFlagsSection(List.ofAll(it)) }

}
