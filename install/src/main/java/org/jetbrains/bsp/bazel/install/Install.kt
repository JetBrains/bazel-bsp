package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.install.cli.CliOptionsProvider
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import kotlin.system.exitProcess

object Install {

    @JvmStatic
    fun main(args: Array<String>) {
        val cliOptionsProvider = CliOptionsProvider(args)
        // TODO .get() wont be needed later (https://youtrack.jetbrains.com/issue/BAZEL-23)
        val cliOptions = cliOptionsProvider.getOptions().get()

        if (cliOptions.helpCliOptions.isHelpOptionUsed) {
            cliOptions.helpCliOptions.printHelp()
        } else if (cliOptions.bloopCliOptions.useBloop) {
            createBloopEnvironmentAndInstallBloopBspServer(cliOptions)
                .onSuccess { printInCaseOfSuccess(cliOptions) }
                .onFailure(::printFailureReasonAndExit1)
        } else {
            createEnvironmentAndInstallBazelBspServer(cliOptions)
                .onSuccess { printInCaseOfSuccess(cliOptions) }
                .onFailure(::printFailureReasonAndExit1)
        }
    }

    private fun createBloopEnvironmentAndInstallBloopBspServer(cliOptions: CliOptions): Try<Void> =
        InstallationContextProvider.parseProjectViewOrGenerateAndSaveAndCreateInstallationContext(cliOptions)
            .flatMap { createBloopEnvironment(it, cliOptions) }

    private fun createBloopEnvironment(installationContext: InstallationContext, cliOptions: CliOptions): Try<Void> {
        val environmentCreator = BloopEnvironmentCreator(cliOptions, installationContext)

        return environmentCreator.create()
    }

    private fun createEnvironmentAndInstallBazelBspServer(cliOptions: CliOptions): Try<Void> =
        InstallationContextProvider.parseProjectViewOrGenerateAndSaveAndCreateInstallationContext(cliOptions)
            .flatMap(::createBspConnectionDetails)
            .flatMap { createEnvironment(it, cliOptions) }

    private fun createBspConnectionDetails(installationContext: InstallationContext): Try<BspConnectionDetails> {
        val bspConnectionDetailsCreator = BspConnectionDetailsCreator(installationContext)

        return bspConnectionDetailsCreator.create()
    }

    private fun createEnvironment(details: BspConnectionDetails, cliOptions: CliOptions): Try<Void> {
        val environmentCreator = BazelBspEnvironmentCreator(cliOptions.workspaceRootDir, details)

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
