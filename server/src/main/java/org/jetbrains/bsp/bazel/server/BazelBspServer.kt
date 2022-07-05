package org.jetbrains.bsp.bazel.server

import ch.epfl.scala.bsp4j.BuildClient
import io.grpc.ServerBuilder
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoResolver
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoStorage
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner.Companion.inCwd
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner.Companion.of
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bep.BepServer
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData
import org.jetbrains.bsp.bazel.server.bsp.BspRequestsRunner
import org.jetbrains.bsp.bazel.server.bsp.BspServerApi
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.bsp.services.CppBuildServerService
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.jetbrains.bsp.bazel.server.common.ServerContainer
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService
import org.jetbrains.bsp.bazel.server.sync.*
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkResolver
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkVersionResolver
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.nio.file.Path

class BazelBspServer(
    bspInfo: BspInfo, workspaceContextProvider: WorkspaceContextProvider, workspaceRoot: Path?
) {
    private val bazelRunner: BazelRunner
    private val bazelInfo: BazelInfo
    private val bspServerApi: BspServerApi
    private val compilationManager: BazelBspCompilationManager
    private val bspClientLogger: BspClientLogger

    init {
        val serverContainer =
            ServerContainer.create(bspInfo, workspaceContextProvider, workspaceRoot, null)
        bspClientLogger = serverContainer.bspClientLogger
        bazelInfo = serverContainer.bazelInfo
        compilationManager = serverContainer.compilationManager
        bazelRunner = serverContainer.bazelRunner
        val bspProjectMapper = BspProjectMapper(
            serverContainer.languagePluginsService, workspaceContextProvider
        )
        val projectSyncService =
            ProjectSyncService(bspProjectMapper, serverContainer.projectProvider)
        val executeService = ExecuteService(
            compilationManager,
            serverContainer.projectProvider,
            bazelRunner,
            workspaceContextProvider
        )
        val cppBuildServerService = CppBuildServerService()
        val serverLifetime = BazelBspServerLifetime()
        val bspRequestsRunner = BspRequestsRunner(serverLifetime)
        bspServerApi = BspServerApi(
            serverLifetime,
            bspRequestsRunner,
            projectSyncService,
            executeService,
            cppBuildServerService
        )
    }

    fun startServer(bspIntegrationData: BspIntegrationData) {
        val launcher = Launcher.Builder<BuildClient>().traceMessages(bspIntegrationData.traceWriter)
            .setOutput(bspIntegrationData.stdout).setInput(bspIntegrationData.stdin)
            .setLocalService(bspServerApi).setRemoteInterface(BuildClient::class.java)
            .setExecutorService(bspIntegrationData.executor).create()
        bspIntegrationData.launcher = launcher
        val client = launcher.remoteProxy
        bspClientLogger.initialize(client)
        val bepServer = BepServer(client, DiagnosticsService(bazelInfo))
        compilationManager.setBepServer(bepServer)
        bspIntegrationData.server = ServerBuilder.forPort(0).addService(bepServer).build()
    }

    fun setBesBackendPort(port: Int) {
        bazelRunner.setBesBackendPort(port)
    }
}
