package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import io.grpc.ServerBuilder;
import io.vavr.control.Option;
import java.nio.file.Path;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.BspRequestsRunner;
import org.jetbrains.bsp.bazel.server.bsp.BspServerApi;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.services.CppBuildServerService;
import org.jetbrains.bsp.bazel.server.common.ServerContainer;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;
import org.jetbrains.bsp.bazel.server.sync.BspProjectMapper;
import org.jetbrains.bsp.bazel.server.sync.ExecuteService;
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService;
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider;

public class BazelBspServer {

  private final BazelRunner bazelRunner;
  private final BazelInfo bazelInfo;
  private final BspServerApi bspServerApi;
  private final BazelBspCompilationManager compilationManager;
  private final BspClientLogger bspClientLogger;

  public BazelBspServer(
      BspInfo bspInfo,
      WorkspaceContextProvider workspaceContextProvider,
      Option<Path> workspaceRoot) {
    var serverContainer =
        ServerContainer.create(bspInfo, workspaceContextProvider, workspaceRoot, Option.none());
    this.bspClientLogger = serverContainer.bspClientLogger;
    this.bazelInfo = serverContainer.bazelInfo;
    this.compilationManager = serverContainer.compilationManager;
    this.bazelRunner = serverContainer.bazelRunner;

    var bspProjectMapper =
        new BspProjectMapper(serverContainer.languagePluginsService, workspaceContextProvider);
    var projectSyncService =
        new ProjectSyncService(bspProjectMapper, serverContainer.projectProvider);
    var executeService =
        new ExecuteService(
            compilationManager,
            serverContainer.projectProvider,
            bazelRunner,
            workspaceContextProvider);
    var cppBuildServerService = new CppBuildServerService();
    var serverLifetime = new BazelBspServerLifetime();
    var bspRequestsRunner = new BspRequestsRunner(serverLifetime);

    this.bspServerApi =
        new BspServerApi(
            serverLifetime,
            bspRequestsRunner,
            projectSyncService,
            executeService,
            cppBuildServerService);
  }

  public void startServer(BspIntegrationData bspIntegrationData) {
    integrateBsp(bspIntegrationData);
  }

  private void integrateBsp(BspIntegrationData bspIntegrationData) {
    var launcher =
        new Launcher.Builder<BuildClient>()
            .traceMessages(bspIntegrationData.getTraceWriter())
            .setOutput(bspIntegrationData.getStdout())
            .setInput(bspIntegrationData.getStdin())
            .setLocalService(bspServerApi)
            .setRemoteInterface(BuildClient.class)
            .setExecutorService(bspIntegrationData.getExecutor())
            .create();

    bspIntegrationData.setLauncher(launcher);
    BuildClient client = launcher.getRemoteProxy();
    this.bspClientLogger.initialize(client);
    var bepServer = new BepServer(client, new DiagnosticsService(bazelInfo));
    compilationManager.setBepServer(bepServer);
    bspIntegrationData.setServer(ServerBuilder.forPort(0).addService(bepServer).build());
  }

  public void setBesBackendPort(int port) {
    bazelRunner.setBesBackendPort(port);
  }
}
