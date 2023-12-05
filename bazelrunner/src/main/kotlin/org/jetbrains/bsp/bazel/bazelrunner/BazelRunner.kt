package org.jetbrains.bsp.bazel.bazelrunner

import java.nio.file.Path
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider

class BazelRunner private constructor(
    private val workspaceContextProvider: WorkspaceContextProvider,
    private val bspClientLogger: BspClientLogger,
    val workspaceRoot: Path?,
) {

    companion object {
        private val LOGGER = LogManager.getLogger(BazelRunner::class.java)

        @JvmStatic
        fun of(
            workspaceContextProvider: WorkspaceContextProvider,
            bspClientLogger: BspClientLogger,
            workspaceRoot: Path?,
        ): BazelRunner {
            return BazelRunner(workspaceContextProvider, bspClientLogger, workspaceRoot)
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
            true
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
    ): BazelProcess {
        val workspaceContext = workspaceContextProvider.currentWorkspaceContext()
        val usedBuildFlags = if (useBuildFlags) buildFlags(workspaceContext) else emptyList()
        val processArgs =
            listOf(bazel(workspaceContext)) + command + usedBuildFlags + flags + arguments
        logInvocation(processArgs, environment, originId)
        val processBuilder = ProcessBuilder(processArgs)
        processBuilder.environment() += environment
        val outputLogger = bspClientLogger.takeIf { parseProcessOutput }
        workspaceRoot?.let { processBuilder.directory(it.toFile()) }
        val process = processBuilder.start()
        return BazelProcess(
            process,
            if (originId == null) outputLogger else outputLogger?.withOriginId(originId),
            originId
        )
    }

    private fun envToString(environment: Map<String, String>): String =
        environment.entries.joinToString(" ") { "${it.key}=${it.value}" }

    private fun logInvocation(processArgs: List<String>, processEnv: Map<String, String>, originId: String?) {
        "Invoking: ${envToString(processEnv)} ${processArgs.joinToString(" ")}"
            .also { LOGGER.info(it) }
            .also { bspClientLogger.withOriginId(originId).message(it) }
    }

    private fun bazel(workspaceContext: WorkspaceContext): String = workspaceContext.bazelBinary.value.toString()
    private fun buildFlags(workspaceContext: WorkspaceContext): List<String> = workspaceContext.buildFlags.values
}
