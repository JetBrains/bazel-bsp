package org.jetbrains.bsp.bazel.server

import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.workspacecontext.DefaultWorkspaceContextProvider
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors
import kotlin.io.path.Path
import kotlin.system.exitProcess

data class CliArgs(val bazelWorkspaceRoot: String, val projectViewPath: String?)

object ServerInitializer {
    @JvmStatic
    fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(
                Thread {
                    ProcessHandle.allProcesses()
                            .filter { it.parent().orElse(null)?.pid() == ProcessHandle.current().pid() }
                            .forEach { it.destroy() }
                }
        )

        val cliArgs = if (args.size > 2 || args.isEmpty()) {
            System.err.printf(
                "Usage: <bazel workspace root> [project view path]%n"
            )
            exitProcess(1)
        } else {
            CliArgs(args.elementAt(0), args.elementAtOrNull(1))
        }
        var hasErrors = false
        val stdout = System.out
        val stdin = System.`in`
        val executor = Executors.newCachedThreadPool()
        try {
            val bspInfo = BspInfo()
            val rootDir = bspInfo.bazelBspDir()
            Files.createDirectories(rootDir)
            val traceFile = rootDir.resolve(Constants.BAZELBSP_TRACE_JSON_FILE_NAME)
            val workspaceContextProvider = getWorkspaceContextProvider(cliArgs.projectViewPath)
            val bspIntegrationData = BspIntegrationData(
                stdout,
                stdin,
                executor,
                createTraceWriterOrNull(traceFile, workspaceContextProvider)
            )
            val bspServer = BazelBspServer(bspInfo, workspaceContextProvider, Path(cliArgs.bazelWorkspaceRoot))
            bspServer.startServer(bspIntegrationData)
            val server = bspIntegrationData.server.start()
            bspServer.setBesBackendPort(server.port)
            bspIntegrationData.launcher.startListening()
            server.awaitTermination()
        } catch (e: Exception) {
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
        workspaceContextProvider: WorkspaceContextProvider
    ): PrintWriter? =
        when (workspaceContextProvider.currentWorkspaceContext().produceTraceLog.value) {
            true -> PrintWriter(
                Files.newOutputStream(
                    traceFile,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            )

            false -> null
        }

    private fun getWorkspaceContextProvider(args: String?): WorkspaceContextProvider {
        val projectViewPath = args?.let { Paths.get(it) }
        return DefaultWorkspaceContextProvider(projectViewPath)
    }
}
