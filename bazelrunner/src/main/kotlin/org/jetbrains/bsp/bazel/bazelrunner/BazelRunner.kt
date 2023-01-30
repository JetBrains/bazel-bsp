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

    private var besBackendPort: Int? = null

    fun commandBuilder(): BazelRunnerCommandBuilder = BazelRunnerCommandBuilder(this)

    fun runBazelCommandBes(
        command: String,
        flags: List<String>,
        arguments: List<String>,
        originId: String?
    ): BazelProcess {
        fun besFlags() = listOf("--bes_backend=grpc://localhost:${besBackendPort!!}")

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
        logInvocation(processArgs, originId)
        val processBuilder = ProcessBuilder(processArgs)
        workspaceRoot?.let { processBuilder.directory(it.toFile()) }
        val process = processBuilder.start()
        return BazelProcess(
            process,
            if (originId == null) bspClientLogger else bspClientLogger.withOriginId(originId),
            originId
        )
    }

    private fun logInvocation(processArgs: List<String>, originId: String?) {
        "Invoking: ${processArgs.joinToString(" ")}"
            .also { LOGGER.info(it) }
            .also { bspClientLogger.withOriginId(originId).message(it) }
    }

    private fun bazel(workspaceContext: WorkspaceContext): String = workspaceContext.bazelPath.value.toString()
    private fun buildFlags(workspaceContext: WorkspaceContext): List<String> = workspaceContext.buildFlags.values

    fun setBesBackendPort(port: Int) {
        besBackendPort = port
    }
}
