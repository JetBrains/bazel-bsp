package org.jetbrains.bsp.bazel.server

import com.jetbrains.bsp.bsp4kt.BuildClient
import com.jetbrains.bsp.bsp4kt.TextDocumentIdentifier
import com.jetbrains.jsonrpc4kt.Launcher
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.jetbrains.bsp.bazel.server.bsp.*
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.common.ServerContainer
import org.jetbrains.bsp.bazel.server.sync.BspProjectMapper
import org.jetbrains.bsp.bazel.server.sync.ExecuteService
import org.jetbrains.bsp.bazel.server.sync.MetricsLogger
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class BazelBspServer(
    bspInfo: BspInfo, workspaceContextProvider: WorkspaceContextProvider, val workspaceRoot: Path, val metricsLogger: MetricsLogger?
) {
    private val bazelRunner: BazelRunner
    private val compilationManager: BazelBspCompilationManager
    private val bspServerApi: BspServerApi
    private val bspClientLogger: BspClientLogger = BspClientLogger()
    private val bspClientTestNotifier: BspClientTestNotifier = BspClientTestNotifier()
    private val bspState: Map<String, Set<TextDocumentIdentifier>> = ConcurrentHashMap()

    init {
        bazelRunner = BazelRunner.of(workspaceContextProvider, this.bspClientLogger, workspaceRoot)
        compilationManager = BazelBspCompilationManager(bazelRunner, bspState)
        bspServerApi = BspServerApi { bspServerData(bspInfo, workspaceContextProvider) }
    }

    private fun bspServerData(bspInfo: BspInfo, workspaceContextProvider: WorkspaceContextProvider): BazelServices {
        val serverContainer = ServerContainer.create(
            bspInfo,
            workspaceContextProvider,
            null,
            bspClientLogger,
            bspClientTestNotifier,
            bazelRunner,
            compilationManager,
            metricsLogger
        )

        val bspProjectMapper = BspProjectMapper(
            serverContainer.languagePluginsService, workspaceContextProvider
        )
        val projectSyncService =
            ProjectSyncService(bspProjectMapper, serverContainer.projectProvider)
        val executeService = ExecuteService(
            compilationManager,
            serverContainer.projectProvider,
            bazelRunner,
            workspaceContextProvider,
            bspClientTestNotifier,
            bspState,
        )
        val serverLifetime = BazelBspServerLifetime()
        val bspRequestsRunner = BspRequestsRunner(serverLifetime)
        return BazelServices(
            serverLifetime,
            bspRequestsRunner,
            projectSyncService,
            executeService
        )
    }

    fun startServer(bspIntegrationData: BspIntegrationData) {
        val launcher = createLauncher(bspIntegrationData).create()
        bspIntegrationData.launcher = launcher
        val client = launcher.remoteProxy
        bspClientLogger.initialize(client)
        bspClientTestNotifier.initialize(client)
        compilationManager.setClient(client)
        compilationManager.setWorkspaceRoot(workspaceRoot)
    }

    private fun createLauncher(bspIntegrationData: BspIntegrationData): Launcher.Builder<BspServerApi, BuildClient> {
        val builder = Launcher.Builder(input = bspIntegrationData.stdin, output = bspIntegrationData.stdout,
            localService = bspServerApi, remoteInterface = BuildClient::class,
            executorService = bspIntegrationData.executor
        )

        // TODO: enable tracing in bsp4kt
//        if (bspIntegrationData.traceWriter != null) {
//            builder.traceMessages(bspIntegrationData.traceWriter)
//        }

        return builder
    }
}
