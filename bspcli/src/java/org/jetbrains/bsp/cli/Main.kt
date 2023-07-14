package org.jetbrains.bsp.cli

import ch.epfl.scala.bsp4j.*
import com.google.common.util.concurrent.ThreadFactoryBuilder

import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.Launcher.Builder
import org.jetbrains.bsp.bazel.install.Install
import org.jetbrains.bsp.bazel.server.BazelBspServer
import org.jetbrains.bsp.bazel.server.sync.BazelBuildServer
import org.jetbrains.bsp.bazel.server.sync.MetricsLogger
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.workspacecontext.DefaultWorkspaceContextProvider
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.io.path.absolute
import kotlin.io.path.writeText


import kotlin.system.exitProcess

interface Api: BuildServer, JvmBuildServer, ScalaBuildServer, JavaBuildServer, CppBuildServer, BazelBuildServer

data class Args(
        val workspace: Path,
        val metricsFile: Path?
)

fun parseArgs(args: Array<String>): Args {
    if (args.size == 1) {
        return Args(workspace = Paths.get(args[0]), metricsFile = null)
    }
    if (args.size == 2) {
        return Args(workspace = Paths.get(args[0]), metricsFile = Paths.get(args[1]))
    }

    println("Invalid number of arguments. Just pass path to your workspace as a CLI argument to this app")
    exitProcess(1)
}

/**
 * The application expects just a single argument - path to your bazel project
 */
fun main(args0: Array<String>) {
    val args = parseArgs(args0)
    val installationDirectory = Files.createTempDirectory("bazelbsp-dir-")
    Install.main(arrayOf(
            "--bazel-workspace", args.workspace.toString(),
            "--directory", installationDirectory.toString(),
            "--targets", "//..."
    ))

    val serverOut = PipedInputStream()
    val serverIn = PrintStream(PipedOutputStream(serverOut), true)
    val clientOut = PipedInputStream()
    val clientIn = PrintStream(PipedOutputStream(clientOut), true)

    val metricsLogger = MetricsLogger(true)
    val serverExecutor = Executors.newFixedThreadPool(4, threadFactory("cli-server-pool-%d"))
    val serverLauncher = startServer(serverIn, clientOut, serverExecutor, args.workspace, installationDirectory, metricsLogger)
    val serverAlveFuture = serverLauncher.startListening()


    val clientExecutor = Executors.newFixedThreadPool(4, threadFactory("cli-client-pool-%d"))
    val clientLauncher = startClient(serverOut, clientIn, clientExecutor)
    val clientAliveFuture = clientLauncher.startListening()

    val proxy = clientLauncher.remoteProxy
    val buildInitializeResponse = proxy.buildInitialize(
            InitializeBuildParams("IntelliJ-BSP",
                    "0.0.1",
                    "2.0.0",
                    args.workspace.toUri().toString(),
                    BuildClientCapabilities(listOf("java")))).get()
    println(buildInitializeResponse)
    proxy.onBuildInitialized()
    proxy.workspaceBuildTargets().get().let { println(it) }
    clientIn.close()
    serverIn.close()

    clientAliveFuture.get()
    serverAlveFuture.get()

    clientExecutor.shutdown()
    serverExecutor.shutdown()

    args.metricsFile?.let {
        it.writeText(metricsLogger.dump())
        println("Metrics dumped to '${it.absolute()}'")
    }
}

private fun threadFactory(nameFormat: String): ThreadFactory =
        ThreadFactoryBuilder()
                .setNameFormat(nameFormat)
                .setUncaughtExceptionHandler { _, e ->
                    e.printStackTrace()
                    exitProcess(1)
                }
                .build()

private fun startClient(serverOut: PipedInputStream, clientIn: PrintStream, clientExecutor: ExecutorService?): Launcher<Api> =
        Builder<Api>()
                .setInput(serverOut)
                .setOutput(clientIn)
                .setRemoteInterface(Api::class.java)
                .setExecutorService(clientExecutor)
                .setLocalService(BuildClient())
                .create()

private fun startServer(serverIn: PrintStream,
                        clientOut: PipedInputStream,
                        serverExecutor: ExecutorService,
                        workspace: Path,
                        directory: Path,
                        metricsLogger: MetricsLogger): Launcher<ch.epfl.scala.bsp4j.BuildClient> {
    val bspInfo = BspInfo(directory)
    val bspIntegrationData = BspIntegrationData(serverIn, clientOut, serverExecutor, null)
    val workspaceContextProvider = DefaultWorkspaceContextProvider(directory.resolve("projectview.bazelproject"))
    val bspServer = BazelBspServer(bspInfo, workspaceContextProvider, workspace, metricsLogger)
    bspServer.startServer(bspIntegrationData)
    return bspIntegrationData.launcher
}


class BuildClient : ch.epfl.scala.bsp4j.BuildClient {


    override fun onBuildShowMessage(params: ShowMessageParams?) {}

    override fun onBuildLogMessage(params: LogMessageParams?) {}

    override fun onBuildTaskStart(params: TaskStartParams?) {}

    override fun onBuildTaskProgress(params: TaskProgressParams?) {}

    override fun onBuildTaskFinish(params: TaskFinishParams?) {}

    override fun onBuildPublishDiagnostics(params: PublishDiagnosticsParams?) {}

    override fun onBuildTargetDidChange(params: DidChangeBuildTarget?) {}

}
