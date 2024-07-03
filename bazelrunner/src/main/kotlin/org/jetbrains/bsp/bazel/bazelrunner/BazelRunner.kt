package org.jetbrains.bsp.bazel.bazelrunner

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.repositoryOverride
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import org.jetbrains.bsp.bazel.workspacecontext.extraFlags
import java.nio.file.Path

class BazelRunner private constructor(
    private val workspaceContextProvider: WorkspaceContextProvider,
    private val bspClientLogger: BspClientLogger,
    val workspaceRoot: Path?,
    val bazelBspRoot: String
) {

    companion object {
        private val LOGGER = LogManager.getLogger(BazelRunner::class.java)

        @JvmStatic
        fun of(
            workspaceContextProvider: WorkspaceContextProvider,
            bspClientLogger: BspClientLogger,
            workspaceRoot: Path?,
            bazelBspRoot: String
        ): BazelRunner {
            return BazelRunner(workspaceContextProvider, bspClientLogger, workspaceRoot, bazelBspRoot)
        }
    }

    fun commandBuilder(): BazelRunnerCommandBuilder = BazelRunnerCommandBuilder(this)

    fun runBazelCommandBes(
        command: List<String>,
        flags: List<String>,
        arguments: List<String>,
        environment: Map<String, String>,
        originId: String?,
        eventTextFile: Path,
        serverPid: Long?,
    ): BazelProcess {
        fun besFlags() = listOf(
            "--build_event_binary_file=${eventTextFile.toAbsolutePath()}",
            "--bes_outerr_buffer_size=10",
            "--isatty=true",
        )

        // TODO https://youtrack.jetbrains.com/issue/BAZEL-617
        return runBazelCommand(
            command,
            flags = besFlags() + flags,
            arguments,
            environment,
            originId,
            true,
        )
    }

    fun runBazelCommand(
        command: List<String>,
        flags: List<String>,
        arguments: List<String>,
        environment: Map<String, String>,
        originId: String?,
        parseProcessOutput: Boolean,
        useBuildFlags: Boolean = true,
        needsServerPid: Boolean = true,
    ): BazelProcess {
        val workspaceContext = workspaceContextProvider.currentWorkspaceContext()
        val usedBuildFlags = if (useBuildFlags) buildFlags(workspaceContext) else emptyList()
        val defaultFlags = listOf(repositoryOverride(Constants.ASPECT_REPOSITORY, bazelBspRoot))
        val processArgs =
            listOf(bazel(workspaceContext)) + command + usedBuildFlags + defaultFlags + flags + arguments
        logInvocation(processArgs, environment, originId)
        val processBuilder = ProcessBuilder(processArgs)
        processBuilder.environment() += environment
        val outputLogger = bspClientLogger.takeIf { parseProcessOutput }?.copy(originId = originId)
        workspaceRoot?.let { processBuilder.directory(it.toFile()) }
        val serverPid = if (needsServerPid) resolveBazelServerPid() else null
        val process = processBuilder.start()
        return BazelProcess(
            process,
            outputLogger,
            serverPid,
        )
    }

    private fun envToString(environment: Map<String, String>): String =
        environment.entries.joinToString(" ") { "${it.key}=${it.value}" }

    private fun logInvocation(processArgs: List<String>, processEnv: Map<String, String>, originId: String?) {
        "Invoking: ${envToString(processEnv)} ${processArgs.joinToString(" ")}"
            .also { LOGGER.info(it) }
            .also { bspClientLogger.copy(originId = originId).message(it) }
    }

    private fun bazel(workspaceContext: WorkspaceContext): String = workspaceContext.bazelBinary.value.toString()
    private fun buildFlags(workspaceContext: WorkspaceContext): List<String> =
        workspaceContext.buildFlags.values + workspaceContext.extraFlags

    private fun resolveBazelServerPid(): Long? {
        val processResult = commandBuilder()
            .info()
            .withArgument("server_pid")
            .executeBazelCommand(needsServerPid = false)
            .waitAndGetResultTimeout()
        return processResult.stdoutLines.firstOrNull()?.toLong()
    }
}
