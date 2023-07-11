package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.CliOptionsProvider
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import java.nio.file.Path
import kotlin.system.exitProcess

object Install {

    @JvmStatic
    fun main(args: Array<String>) {
        val cliOptionsProvider = CliOptionsProvider(args)
        val cliOptions = cliOptionsProvider.getOptions().get()

        if (cliOptions.helpCliOptions.isHelpOptionUsed) {
            cliOptions.helpCliOptions.printHelp()
        } else {
            runInstall(cliOptions)
        }
    }

    private fun runInstall(cliOptions: CliOptions) {
        try {
            runInstallOrThrow(cliOptions, cliOptions.projectViewCliOptions?.produceTraceLog ?: false)
        } catch (e: Exception) {
            System.err.print("Bazel BSP server installation failed! Reason: ${e.stackTrace}")
            exitProcess(1)
        }
    }

    private fun runInstallOrThrow(cliOptions: CliOptions, createTraceLog: Boolean) {
        val installationContext = InstallationContextProvider.createInstallationContext(cliOptions)
        InstallationContextProvider.generateAndSaveProjectViewFile(cliOptions)

        val connectionDetails = createBspConnectionDetails(installationContext, createTraceLog)
        createEnvironment(connectionDetails, cliOptions)

        printSuccess(cliOptions.workspaceRootDir)
    }

    private fun createBspConnectionDetails(installationContext: InstallationContext, createTraceLog: Boolean): BspConnectionDetails {
        val bspConnectionDetailsCreator = BspConnectionDetailsCreator(installationContext, createTraceLog)
        return bspConnectionDetailsCreator.create().get()
    }

    private fun createEnvironment(details: BspConnectionDetails, cliOptions: CliOptions) {
        val environmentCreator = BazelBspEnvironmentCreator(cliOptions.workspaceRootDir, details)
        environmentCreator.create().get()
    }

    private fun printSuccess(workspaceRootDir: Path) {
        val absoluteDirWhereServerWasInstalledIn = workspaceRootDir.toAbsolutePath().normalize()
        println("Bazel BSP server installed in '$absoluteDirWhereServerWasInstalledIn'.")
    }
}
