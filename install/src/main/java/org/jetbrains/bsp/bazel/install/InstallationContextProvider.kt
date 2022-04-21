package org.jetbrains.bsp.bazel.install

import io.vavr.control.Option
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextConstructor
import java.nio.file.Path

object InstallationContextProvider {

    private const val RESOURCES_PROJECT_VIEW_FILE_PATH = "/default-projectview.bazelproject"
    private const val DEFAULT_GENERATED_PROJECT_VIEW_FILE_NAME = "projectview.bazelproject"

    fun parseProjectViewOrGenerateAndSaveAndCreateInstallationContext(cliOptions: CliOptions): Try<InstallationContext> =
        if (cliOptions.projectViewCliOptions == null) parseProjectViewAndCreateInstallationContext(cliOptions.projectViewFilePath)
        else generateAndSaveProjectViewAndCreateInstallationContext(cliOptions)

    private fun parseProjectViewAndCreateInstallationContext(projectViewFilePath: Path?): Try<InstallationContext> {
        val projectViewProvider =
            ProjectViewDefaultFromResourcesProvider(projectViewFilePath, RESOURCES_PROJECT_VIEW_FILE_PATH)
        val projectViewTry = projectViewProvider.create()

        val installationContextConstructor = InstallationContextConstructor(Option.of(projectViewFilePath))
        return installationContextConstructor.construct(projectViewTry)
    }

    private fun generateAndSaveProjectViewAndCreateInstallationContext(cliOptions: CliOptions): Try<InstallationContext> {
        val generatedProjectViewFilePath = calculateGeneratedProjectViewPath(cliOptions)
        val projectViewTry =
            ProjectViewCLiOptionsProvider.generateProjectViewAndSave(cliOptions, generatedProjectViewFilePath)

        val installationContextConstructor = InstallationContextConstructor(Option.of(generatedProjectViewFilePath))
        return installationContextConstructor.construct(projectViewTry)
    }

    private fun calculateGeneratedProjectViewPath(cliOptions: CliOptions): Path =
        cliOptions.projectViewFilePath ?: cliOptions.workspaceRootDir.resolve(DEFAULT_GENERATED_PROJECT_VIEW_FILE_NAME)
}
