package org.jetbrains.bsp.cli

import ch.epfl.scala.bsp4j.*
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.Launcher.Builder
import org.jetbrains.bsp.bazel.install.Install
import org.jetbrains.bsp.bazel.server.BazelBspServer
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.workspacecontext.DefaultWorkspaceContextProvider
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * The application expects just a single argument - path to your bazel project
 */
fun main(args: Array<String>) {
    if(args.size != 1) {
        println("Invalid number of arguments. Just pass path to your workspace as a CLI argument to this app")
        System.exit(1)
    }
    val workspace = Paths.get(args[0])
    Install.main(arrayOf(
            "--bazel-workspace", workspace.toString(),
            "--directory", workspace.toString(),
    ))

    val serverOut = PipedInputStream()
    val serverIn = PrintStream(PipedOutputStream(serverOut), true)
    val clientOut = PipedInputStream()
    val clientIn = PrintStream(PipedOutputStream(clientOut), true)

    val serverExecutor = Executors.newFixedThreadPool(4)
    val serverLauncher = startServer(serverIn, clientOut, serverExecutor, workspace)
    val serverAlveFuture = serverLauncher.startListening()


    val clientExecutor = Executors.newFixedThreadPool(4)
    val clientLauncher = startClient(serverOut, clientIn, clientExecutor)
    val clientAliveFuture = clientLauncher.startListening()

    val proxy = clientLauncher.remoteProxy
    val buildInitializeResponse = proxy.buildInitialize(
            InitializeBuildParams("IntelliJ-BSP",
                    "0.0.1",
                    "2.0.0",
                    workspace.toUri().toString(),
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
}

private fun startClient(serverOut: PipedInputStream, clientIn: PrintStream, clientExecutor: ExecutorService?): Launcher<BuildServer> =
        Builder<BuildServer>()
                .setInput(serverOut)
                .setOutput(clientIn)
                .setRemoteInterface(BuildServer::class.java)
                .setExecutorService(clientExecutor)
                .setLocalService(BuildClient())
                .create()

private fun startServer(serverIn: PrintStream,
                        clientOut: PipedInputStream,
                        serverExecutor: ExecutorService,
                        workspace: Path): Launcher<ch.epfl.scala.bsp4j.BuildClient> {
    val bspInfo = BspInfo(workspace)
    val bspIntegrationData = BspIntegrationData(serverIn, clientOut, serverExecutor, null)
    val workspaceContextProvider = DefaultWorkspaceContextProvider(workspace.resolve("projectview.bazelproject"))
    val bspServer = BazelBspServer(bspInfo, workspaceContextProvider, workspace)
    bspServer.startServer(bspIntegrationData)
    return bspIntegrationData.launcher
}



class BuildClient : ch.epfl.scala.bsp4j.BuildClient {
    override fun onBuildShowMessage(params: ShowMessageParams?) {
        TODO("Not yet implemented")
    }

    override fun onBuildLogMessage(params: LogMessageParams?) {
        println(params)
    }

    override fun onBuildTaskStart(params: TaskStartParams?) {
        TODO("Not yet implemented")
    }

    override fun onBuildTaskProgress(params: TaskProgressParams?) {
        TODO("Not yet implemented")
    }

    override fun onBuildTaskFinish(params: TaskFinishParams?) {
        TODO("Not yet implemented")
    }

    override fun onBuildPublishDiagnostics(params: PublishDiagnosticsParams?) {
        TODO("Not yet implemented")
    }

    override fun onBuildTargetDidChange(params: DidChangeBuildTarget?) {
        TODO("Not yet implemented")
    }

}
