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

        private val CARGO_BAZEL_REPIN_ENV_VAR = "CARGO_BAZEL_REPIN" to "1"

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
        command: String,
        flags: List<String>,
        arguments: List<String>,
        originId: String?,
        besBackendPort: Int,
    ): BazelProcess {
        fun besFlags() = listOf("--bes_backend=grpc://localhost:${besBackendPort}")

        return runBazelCommand(command, flags = besFlags() + flags, arguments, originId)
    }

    fun runBazelCommand(
        command: String,
        flags: List<String>,
        arguments: List<String>,
        originId: String? = null
    ): BazelProcess {
        val workspaceContext = workspaceContextProvider.currentWorkspaceContext()

        val flagsWithOrigin = if (originId != null) flags + "--define=ORIGINID=$originId" else flags
        val processArgs = listOf(bazel(workspaceContext), command) + buildFlags(workspaceContext) + flagsWithOrigin + arguments
        val processEnv = mapOf(CARGO_BAZEL_REPIN_ENV_VAR)
        logInvocation(processArgs, processEnv, originId)
        val processBuilder = ProcessBuilder(processArgs)
        processBuilder.environment() += processEnv
        workspaceRoot?.let { processBuilder.directory(it.toFile()) }
        val process = processBuilder.start()
        return BazelProcess(
            process,
            if (originId == null) bspClientLogger else bspClientLogger.withOriginId(originId),
            originId
        )
    }

    private fun envToString(processEnv: Map<String, String>): String =
        processEnv.entries.joinToString(" ") { "${it.key}=${it.value}" }

    private fun logInvocation(processArgs: List<String>, processEnv: Map<String, String>, originId: String?) {
        "Invoking: ${envToString(processEnv)} ${processArgs.joinToString(" ")}"
            .also { LOGGER.info(it) }
            .also { bspClientLogger.withOriginId(originId).message(it) }
    }

    private fun bazel(workspaceContext: WorkspaceContext): String = workspaceContext.bazelPath.value.toString()
    private fun buildFlags(workspaceContext: WorkspaceContext): List<String> = workspaceContext.buildFlags.values
}
