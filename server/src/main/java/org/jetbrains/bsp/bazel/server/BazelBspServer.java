package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import io.grpc.ServerBuilder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoResolver;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.BspRequestsRunner;
import org.jetbrains.bsp.bazel.server.bsp.BspServerApi;
import org.jetbrains.bsp.bazel.server.bsp.config.BazelBspServerConfig;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.services.CppBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.BazelProjectMapper;
import org.jetbrains.bsp.bazel.server.sync.BepServerProjectListener;
import org.jetbrains.bsp.bazel.server.sync.BspProjectMapper;
import org.jetbrains.bsp.bazel.server.sync.ExecuteService;
import org.jetbrains.bsp.bazel.server.sync.ProjectProvider;
import org.jetbrains.bsp.bazel.server.sync.ProjectResolver;
import org.jetbrains.bsp.bazel.server.sync.ProjectStorage;
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService;
import org.jetbrains.bsp.bazel.server.sync.ProjectViewProvider;
import org.jetbrains.bsp.bazel.server.sync.TargetKindResolver;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService;
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin;

public class BazelBspServer {

  private final BazelBspServerConfig bazelBspServerConfig;
  private final BazelRunner bazelRunner;
  private final BazelInfo bazelInfo;

  private BspServerApi bspServerApi;
  private BazelBspCompilationManager compilationManager;
  private ProjectProvider projectProvider;
  private final BspClientLogger bspClientLogger;

  public BazelBspServer(BazelBspServerConfig bazelBspServerConfig) {
    this.bazelBspServerConfig = bazelBspServerConfig;
    var bazelPath = bazelBspServerConfig.getBazelPath();
    this.bspClientLogger = new BspClientLogger();
    var bazelDataResolver = new BazelInfoResolver(BazelRunner.inCwd(bazelPath, bspClientLogger));
    this.bazelInfo = bazelDataResolver.resolveBazelInfo();
    this.bazelRunner = BazelRunner.of(bazelPath, bspClientLogger, bazelInfo);
  }

  public void startServer(BspIntegrationData bspIntegrationData) {
    var serverLifetime = new BazelBspServerLifetime();
    var bspRequestsRunner = new BspRequestsRunner(serverLifetime);

    var bspInfo = new BspInfo();
    this.compilationManager = new BazelBspCompilationManager(bazelRunner, bazelInfo);
    var aspectsResolver = new InternalAspectsResolver(bazelInfo, bspInfo);
    var bazelBspAspectsManager = new BazelBspAspectsManager(compilationManager, aspectsResolver);
    var bazelPathsResolver = new BazelPathsResolver(bazelInfo);
    var javaLanguagePlugin = new JavaLanguagePlugin(bazelPathsResolver, bazelInfo);
    var scalaLanguagePlugin = new ScalaLanguagePlugin(javaLanguagePlugin, bazelPathsResolver);
    var cppLanguagePlugin = new CppLanguagePlugin();
    var languagePluginsService =
        new LanguagePluginsService(scalaLanguagePlugin, javaLanguagePlugin, cppLanguagePlugin);
    var targetKindResolver = new TargetKindResolver();
    var bazelProjectMapper =
        new BazelProjectMapper(languagePluginsService, bazelPathsResolver, targetKindResolver);
    var projectViewProvider = new ProjectViewProvider(bazelBspServerConfig.getProjectView());
    var projectResolver =
        new ProjectResolver(
            bazelBspAspectsManager, projectViewProvider, bazelProjectMapper, bspClientLogger);
    var projectStorage = new ProjectStorage(bspInfo, bspClientLogger);
    this.projectProvider = new ProjectProvider(projectResolver, projectStorage);
    var bspProjectMapper = new BspProjectMapper(languagePluginsService);
    var projectSyncService = new ProjectSyncService(bspProjectMapper, projectProvider);
    var executeService = new ExecuteService(compilationManager, projectProvider, bazelRunner);
    var cppBuildServerService = new CppBuildServerService();

    this.bspServerApi =
        new BspServerApi(
            serverLifetime,
            bspRequestsRunner,
            projectSyncService,
            executeService,
            cppBuildServerService);

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
    var bepServer = new BepServer(bazelInfo, client, bspClientLogger);
    compilationManager.setBepServer(bepServer);
    projectProvider.addListener(new BepServerProjectListener(bepServer));
    bspIntegrationData.setServer(ServerBuilder.forPort(0).addService(bepServer).build());
  }

  public void setBesBackendPort(int port) {
    bazelRunner.setBesBackendPort(port);
  }
}
