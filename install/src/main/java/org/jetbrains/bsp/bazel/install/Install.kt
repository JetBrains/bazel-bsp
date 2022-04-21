package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.CliOptionsProvider
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextConstructor
import kotlin.system.exitProcess

object Install {

    @JvmStatic
    fun main(args: Array<String>) {
        val cliOptionsProvider = CliOptionsProvider(args)
        // TODO .get() wont be needed later (https://youtrack.jetbrains.com/issue/BAZEL-23)
        val cliOptions = cliOptionsProvider.getOptions().get()
        if (cliOptions.projectViewCliOptions != null) {
            val projectViewCliOptions = ProjectViewCLiOptionsProvider()
            projectViewCliOptions.generateProjectViewAndSave(cliOptions)
        } else {
            val default = ProjectViewDefaultInstallerProvider()
            default.parseProjectViewOrGenerateAndSave(cliOptions)
        }
        if (cliOptions.helpCliOptions.isHelpOptionUsed) {
            cliOptions.helpCliOptions.printHelp()
        } else {
            createEnvironmentAndInstallBazelBspServer(cliOptions)
                    .onSuccess { printInCaseOfSuccess(cliOptions) }
                    .onFailure(::printFailureReasonAndExit1)
        }
    }
    private fun createEnvironmentAndInstallBazelBspServer(cliOptions: CliOptions): Try<Void> =
            constructInstallationContext(cliOptions)
                    .flatMap { createBspConnectionDetails(it, cliOptions) }
                    .flatMap { createEnvironment(it, cliOptions) }

    private fun constructInstallationContext(cliOptions: CliOptions): Try<InstallationContext> {
        val resourcesProjectViewFilePath = "/default-projectview.bazelproject"
        val projectViewProvider = ProjectViewDefaultFromResourcesProvider(
                cliOptions.projectViewFilePath, resourcesProjectViewFilePath)
        val projectViewTry = projectViewProvider.create()

        val installationContextConstructor = InstallationContextConstructor()
        return installationContextConstructor.construct(projectViewTry)
    }

    private fun createBspConnectionDetails(
            installationContext: InstallationContext, cliOptions: CliOptions): Try<BspConnectionDetails> {
        val bspConnectionDetailsCreator = BspConnectionDetailsCreator(installationContext, cliOptions.projectViewFilePath)

        return bspConnectionDetailsCreator.create()
    }

    private fun createEnvironment(details: BspConnectionDetails, cliOptions: CliOptions): Try<Void> {
        val environmentCreator = EnvironmentCreator(cliOptions.workspaceRootDir, details)

        return environmentCreator.create()
    }

    private fun printInCaseOfSuccess(cliOptions: CliOptions) {
        val absoluteDirWhereServerWasInstalledIn = cliOptions.workspaceRootDir.toAbsolutePath().normalize()
        println("Bazel BSP server installed in '$absoluteDirWhereServerWasInstalledIn'.")
    }

    private fun printFailureReasonAndExit1(exception: Throwable) {
        System.err.print("Bazel BSP server installation failed! Reason: ")
        exception.printStackTrace()
        exitProcess(1)
    }
}
