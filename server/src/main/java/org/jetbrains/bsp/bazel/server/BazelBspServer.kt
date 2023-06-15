package org.jetbrains.bsp.bazel.server

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.Launcher
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

    private fun createLauncher(bspIntegrationData: BspIntegrationData): Launcher.Builder<BuildClient> {
        val builder = Launcher.Builder<BuildClient>()
            .setOutput(bspIntegrationData.stdout).setInput(bspIntegrationData.stdin)
            .setLocalService(bspServerApi).setRemoteInterface(BuildClient::class.java)
            .setExecutorService(bspIntegrationData.executor)

        if (bspIntegrationData.traceWriter != null) {
            builder.traceMessages(bspIntegrationData.traceWriter)
        }

        return builder
    }
}
