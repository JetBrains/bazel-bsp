package org.jetbrains.bsp.bazel.server

import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.workspacecontext.DefaultWorkspaceContextProvider
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors
import kotlin.system.exitProcess

data class CliArgs(val bazelWorkspaceRoot: String?, val projectViewPath: String?)

object ServerInitializer {
    @JvmStatic
    fun main(args: Array<String>) {
        val cliArgs = if (args.size > 2) {
            System.err.printf(
                "Expected optional path to project view file; got too many args: %s%n",
                args.contentToString()
            )
            exitProcess(1)
        } else {
            CliArgs(args.elementAtOrNull(0), args.elementAtOrNull(1))
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
            val traceWriter = PrintWriter(
                Files.newOutputStream(
                    traceFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                )
            )
            val workspaceContextProvider = getWorkspaceContextProvider(cliArgs.projectViewPath)
            val bspIntegrationData = BspIntegrationData(stdout, stdin, executor, traceWriter)
            val bspServer = BazelBspServer(
                bspInfo, workspaceContextProvider,
                cliArgs.bazelWorkspaceRoot?.let { Paths.get(it) }
            )
            bspServer.startServer(bspIntegrationData)
            val server = bspIntegrationData.server.start()
            bspServer.setBesBackendPort(server.port)
            bspIntegrationData.launcher.startListening()
            server.awaitTermination()
        } catch (e: Exception) {
            e.printStackTrace()
            hasErrors = true
        } finally {
            executor.shutdown()
        }
        if (hasErrors) {
            exitProcess(1)
        }
    }

    private fun getWorkspaceContextProvider(args: String?): WorkspaceContextProvider {
        val projectViewPath = args?.let { Paths.get(it) }
        return DefaultWorkspaceContextProvider(projectViewPath)
    }
}
