package org.jetbrains.bsp.bazel.install

import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextDebuggerAddressEntity
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextJavaPathEntity
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextJavaPathEntityMapper
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

object InstallationContextProvider {

    private const val DEFAULT_GENERATED_PROJECT_VIEW_FILE_NAME = "projectview.bazelproject"

    fun createInstallationContext(cliOptions: CliOptions): InstallationContext =
        InstallationContext(
            javaPath = cliOptions
                .javaPath
                ?.let { InstallationContextJavaPathEntity(it) } ?: InstallationContextJavaPathEntityMapper.default(),
            debuggerAddress = cliOptions
                .debuggerAddress
                ?.let { InstallationContextDebuggerAddressEntity(it) },
            projectViewFilePath = calculateGeneratedProjectViewPath(cliOptions),
            bazelWorkspaceRootDir = cliOptions.bazelWorkspaceRootDir
        )

    fun generateAndSaveProjectViewFileIfNeeded(cliOptions: CliOptions) {
        val generatedProjectViewFilePath = calculateGeneratedProjectViewPath(cliOptions)
        if (!generatedProjectViewFilePath.isFileExisted() || cliOptions.projectViewCliOptions != null) {
            ProjectViewCLiOptionsProvider.generateProjectViewAndSave(cliOptions, generatedProjectViewFilePath)
        }
    }

    private fun calculateGeneratedProjectViewPath(cliOptions: CliOptions): Path =
        cliOptions.projectViewFilePath ?: cliOptions.workspaceRootDir.resolve(DEFAULT_GENERATED_PROJECT_VIEW_FILE_NAME)

    private fun Path.isFileExisted() = this.exists() && this.isRegularFile()
}
