package org.jetbrains.bsp.cli

import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.BuildServer
import ch.epfl.scala.bsp4j.CppBuildServer
import ch.epfl.scala.bsp4j.DidChangeBuildTarget
import ch.epfl.scala.bsp4j.InitializeBuildParams
import ch.epfl.scala.bsp4j.JavaBuildServer
import ch.epfl.scala.bsp4j.JvmBuildServer
import ch.epfl.scala.bsp4j.LogMessageParams
import ch.epfl.scala.bsp4j.PrintParams
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.ScalaBuildServer
import ch.epfl.scala.bsp4j.ShowMessageParams
import ch.epfl.scala.bsp4j.TaskFinishParams
import ch.epfl.scala.bsp4j.TaskProgressParams
import ch.epfl.scala.bsp4j.TaskStartParams
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.Launcher.Builder
import org.jetbrains.bsp.BazelBuildServer
import org.jetbrains.bsp.bazel.install.Install
import org.jetbrains.bsp.bazel.server.BazelBspServer
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.sync.MetricsLogger
import org.jetbrains.bsp.bazel.workspacecontext.DefaultWorkspaceContextProvider
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.absolute
import kotlin.io.path.writeText
import kotlin.system.exitProcess

interface Api: BuildServer, JvmBuildServer, ScalaBuildServer, JavaBuildServer, CppBuildServer, BazelBuildServer

data class Args(
        val workspace: Path,
        val metricsFile: Path?,
        val target: String
)

fun parseArgs(args: Array<String>): Args {
    if (args.size == 1) {
        return Args(workspace = Paths.get(args[0]), metricsFile = null, target = "//...")
    }
    if (args.size == 2) {
        return Args(workspace = Paths.get(args[0]), metricsFile = Paths.get(args[1]), target = "//...")
    }
    if (args.size == 3) {
        return Args(workspace = Paths.get(args[0]), metricsFile = Paths.get(args[1]), target = args[2])
    }


    println("Invalid number of arguments. Just pass path to your workspace as a CLI argument to this app")
    exitProcess(1)
}

/**
 * The application expects just a single argument - path to your bazel project
 */
fun main(args0: Array<String>) {
    val args = parseArgs(args0)
    val attrs = PosixFilePermissions.asFileAttribute(
            setOf(PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_EXECUTE))
    val installationDirectory = Files.createTempDirectory("bazelbsp-dir-", attrs)
    Install.main(arrayOf(
            "--bazel-workspace", args.workspace.toString(),
            "--directory", installationDirectory.toString(),
            "--targets", args.target
    ))

    val serverOut = FixedThreadPipedOutputStream()
    val clientOut = FixedThreadPipedOutputStream()

    val metricsLogger = MetricsLogger(true)
    val serverExecutor = Executors.newFixedThreadPool(4, threadFactory("cli-server-pool-%d"))
    val serverLauncher = startServer(serverOut, clientOut.inputStream, serverExecutor, args.workspace, installationDirectory, metricsLogger)
    val serverAlveFuture = serverLauncher.startListening()

    val clientExecutor = Executors.newFixedThreadPool(4, threadFactory("cli-client-pool-%d"))
    val clientLauncher = startClient(serverOut.inputStream, clientOut, clientExecutor)
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

    clientExecutor.shutdown()
    serverExecutor.shutdown()

    clientOut.stop()
    serverOut.stop()

    clientAliveFuture.get()
    serverAlveFuture.get()

    args.metricsFile?.let {
        it.writeText(metricsLogger.dump())
        println("Metrics dumped to '${it.absolute()}'")
    }
}

/**
 * This class is required, because of limitations of java's PipedStreams.
 * Unfortunately, whenever PipedInputStream calls read ([code](https://github.com/openjdk/jdk/blob/e30e3564420c631f08ac3d613ab91c93227a00b3/src/java.base/share/classes/java/io/PipedInputStream.java#L314-L316)),
 * it checks whether the writing thread is alive. Unfortunately, in case of Bazel BSP server, there are a lot of writes
 * from different threads, that are often spawned only temporarily, from java's Executors.
 *
 * The idea how to solve it is to create a single thread, which lifetime is longer than both PipedOutputStream and
 * PipedInputStream, and it's the only thread that is allowed to write to PipedOutputStream
 */
class FixedThreadPipedOutputStream : OutputStream() {
    val inputStream = PipedInputStream()
    private val outputStream = PrintStream(PipedOutputStream(inputStream), true)
    private val queue = ArrayBlockingQueue<Int>(10000)
    private val _stop = AtomicBoolean(false)
    private val thread = Thread {
        while (!_stop.get()) {
            queue.poll(100, TimeUnit.MILLISECONDS)
                    ?.let { outputStream.write(it) }
        }
    }.also { it.start() }

    fun stop() {
        outputStream.close()
        inputStream.close()
        _stop.set(true)
        thread.join()
    }

    override fun write(b: Int) {
        queue.put(b)
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

private fun startClient(serverOut: PipedInputStream, clientIn: OutputStream, clientExecutor: ExecutorService?): Launcher<Api> =
        Builder<Api>()
                .setInput(serverOut)
                .setOutput(clientIn)
                .setRemoteInterface(Api::class.java)
                .setExecutorService(clientExecutor)
                .setLocalService(BuildClient())
                .create()

private fun startServer(serverIn: OutputStream,
                        clientOut: PipedInputStream,
                        serverExecutor: ExecutorService,
                        workspace: Path,
                        directory: Path,
                        metricsLogger: MetricsLogger): Launcher<ch.epfl.scala.bsp4j.BuildClient> {
    val bspInfo = BspInfo(directory)
    val bspIntegrationData = BspIntegrationData(serverIn, clientOut, serverExecutor, null)
    val workspaceContextProvider = DefaultWorkspaceContextProvider(
      workspaceRoot = workspace,
      projectViewPath = directory.resolve("projectview.bazelproject")
    )
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

    override fun onRunPrintStdout(p0: PrintParams?) {}

    override fun onRunPrintStderr(p0: PrintParams?) {}

    override fun onBuildPublishDiagnostics(params: PublishDiagnosticsParams?) {}

    override fun onBuildTargetDidChange(params: DidChangeBuildTarget?) {}
}
