package org.jetbrains.bsp.bazel.server

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.benchmark.TelemetryConfig
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.workspacecontext.DefaultWorkspaceContextProvider
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors
import kotlin.io.path.Path
import kotlin.system.exitProcess

data class CliArgs(
    val bazelWorkspaceRoot: String,
    val projectViewPath: String,
    val produceTraceLog: Boolean,
)

private val log = LogManager.getLogger(ServerInitializer::class.java)

object ServerInitializer {
    @JvmStatic
    fun main(args: Array<String>) {
        log.info("Starting server with args: ${args.toList()}")

        Runtime.getRuntime().addShutdownHook(
                Thread {
                    ProcessHandle.allProcesses()
                            .filter { it.parent().orElse(null)?.pid() == ProcessHandle.current().pid() }
                            .forEach { it.destroy() }
                }
        )

        val cliArgs = if (args.size != 3) {
            log.error("Wrong number of args, exiting with exit code 1")
            System.err.println("Usage: <bazel workspace root> <project view path> <produce trace log flag>")
            exitProcess(1)
        } else {
            CliArgs(
                bazelWorkspaceRoot = args.elementAt(0),
                projectViewPath = args.elementAt(1),
                produceTraceLog = args.elementAt(2).toBoolean(),
            )
        }
        var hasErrors = false
        val stdout = System.out
        val stdin = System.`in`
        val executor = Executors.newCachedThreadPool()
        try {
            log.info("Initializing server")
            val bspInfo = BspInfo()
            val rootDir = bspInfo.bazelBspDir()
            Files.createDirectories(rootDir)
            val traceFile = rootDir.resolve(Constants.BAZELBSP_TRACE_JSON_FILE_NAME)
            val workspaceContextProvider = DefaultWorkspaceContextProvider(
                workspaceRoot = Path(cliArgs.bazelWorkspaceRoot),
                projectViewPath = Path(cliArgs.projectViewPath)
            )
            val bspIntegrationData = BspIntegrationData(
                stdout,
                stdin,
                executor,
                createTraceWriterOrNull(traceFile, cliArgs.produceTraceLog)
            )
            val bspServer = BazelBspServer(bspInfo, workspaceContextProvider, Path(cliArgs.bazelWorkspaceRoot), TelemetryConfig())
            val launcher = bspServer.buildServer(bspIntegrationData)
            launcher.startListening().get()
            log.info("Server has been initialized")
        } catch (e: Exception) {
            log.error("Server initialization failed", e)
            e.printStackTrace(System.err)
            hasErrors = true
        } finally {
            executor.shutdown()
        }
        if (hasErrors) {
            exitProcess(1)
        }
    }

    private fun createTraceWriterOrNull(
        traceFile: Path,
        createTraceFile: Boolean,
    ): PrintWriter? =
        if (createTraceFile) {
            PrintWriter(
                Files.newOutputStream(
                    traceFile,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            )
        } else null
}
