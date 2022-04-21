package org.jetbrains.bsp.bazel.install

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path

class ProjectViewDefaultInstallerProvider {

    fun parseProjectViewOrGenerateAndSave(cliOptions: CliOptions): Try<ProjectView> =
            if (cliOptions.projectViewCliOptions == null) defaultProjectViewFromResourcesProvider(cliOptions.projectViewFilePath)
            else projectViewProvideAndSaveFromCliOptions(cliOptions)

    private fun defaultProjectViewFromResourcesProvider(projectViewFilePath: Path?): Try<ProjectView> {
        val projectViewProvider = ProjectViewDefaultFromResourcesProvider(projectViewFilePath, resourcesProjectViewFilePath)

        return projectViewProvider.create()
    }

    private fun projectViewProvideAndSaveFromCliOptions(cliOptions: CliOptions): Try<ProjectView> {
        val provider = ProjectViewCLiOptionsProvider()

        return provider.generateProjectViewAndSave(cliOptions)
    }

    private companion object {
        private const val resourcesProjectViewFilePath = "/default-projectview.bazelproject"
    }
}
