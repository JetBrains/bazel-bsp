package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import io.grpc.ServerBuilder;
import io.vavr.control.Option;
import java.nio.file.Path;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoResolver;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoStorage;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.BspRequestsRunner;
import org.jetbrains.bsp.bazel.server.bsp.BspServerApi;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.services.CppBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.BazelProjectMapper;
import org.jetbrains.bsp.bazel.server.sync.BspProjectMapper;
import org.jetbrains.bsp.bazel.server.sync.ExecuteService;
import org.jetbrains.bsp.bazel.server.sync.FileProjectStorage;
import org.jetbrains.bsp.bazel.server.sync.ProjectProvider;
import org.jetbrains.bsp.bazel.server.sync.ProjectResolver;
import org.jetbrains.bsp.bazel.server.sync.ProjectStorage;
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService;
import org.jetbrains.bsp.bazel.server.sync.TargetKindResolver;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService;
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkResolver;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkVersionResolver;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin;
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
        makeProjectProvider(bspInfo, workspaceContextProvider, workspaceRoot, Option.none());
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

  public static ServerContainer makeProjectProvider(
      BspInfo bspInfo,
      WorkspaceContextProvider workspaceContextProvider,
      Option<Path> workspaceRoot,
      Option<ProjectStorage> projectStorage) {
    var bspClientLogger = new BspClientLogger();
    var bazelInfoStorage = new BazelInfoStorage(bspInfo);
    var bazelDataResolver =
        workspaceRoot
            .map(
                root ->
                    new BazelInfoResolver(
                        BazelRunner.of(workspaceContextProvider, bspClientLogger, root),
                        bazelInfoStorage))
            .getOrElse(
                () ->
                    new BazelInfoResolver(
                        BazelRunner.inCwd(workspaceContextProvider, bspClientLogger),
                        bazelInfoStorage));

    var bazelInfo = bazelDataResolver.resolveBazelInfo();
    var bazelRunner =
        BazelRunner.of(workspaceContextProvider, bspClientLogger, bazelInfo.getWorkspaceRoot());
    var compilationManager = new BazelBspCompilationManager(bazelRunner);
    var aspectsResolver = new InternalAspectsResolver(bazelInfo, bspInfo);
    var bazelBspAspectsManager = new BazelBspAspectsManager(compilationManager, aspectsResolver);
    var bazelPathsResolver = new BazelPathsResolver(bazelInfo);
    var jdkResolver = new JdkResolver(bazelPathsResolver, new JdkVersionResolver());
    var javaLanguagePlugin = new JavaLanguagePlugin(bazelPathsResolver, jdkResolver, bazelInfo);
    var scalaLanguagePlugin = new ScalaLanguagePlugin(javaLanguagePlugin, bazelPathsResolver);
    var cppLanguagePlugin = new CppLanguagePlugin();
    var thriftLanguagePlugin = new ThriftLanguagePlugin(bazelPathsResolver);
    var languagePluginsService =
        new LanguagePluginsService(
            scalaLanguagePlugin, javaLanguagePlugin, cppLanguagePlugin, thriftLanguagePlugin);
    var targetKindResolver = new TargetKindResolver();
    var bazelProjectMapper =
        new BazelProjectMapper(languagePluginsService, bazelPathsResolver, targetKindResolver);
    var projectResolver =
        new ProjectResolver(
            bazelBspAspectsManager, workspaceContextProvider, bazelProjectMapper, bspClientLogger);
    var finalProjectStorage =
        projectStorage.getOrElse(() -> new FileProjectStorage(bspInfo, bspClientLogger));
    var projectProvider = new ProjectProvider(projectResolver, finalProjectStorage);

    return new ServerContainer(
        projectProvider,
        bspClientLogger,
        bazelInfo,
        bazelRunner,
        compilationManager,
        languagePluginsService);
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

  public static class ServerContainer {
    public final ProjectProvider projectProvider;
    public final BspClientLogger bspClientLogger;
    public final BazelInfo bazelInfo;
    public final BazelRunner bazelRunner;
    public final BazelBspCompilationManager compilationManager;
    public final LanguagePluginsService languagePluginsService;

    ServerContainer(
        ProjectProvider projectProvider,
        BspClientLogger bspClientLogger,
        BazelInfo bazelInfo,
        BazelRunner bazelRunner,
        BazelBspCompilationManager compilationManager,
        LanguagePluginsService languagePluginsService) {
      this.projectProvider = projectProvider;
      this.bspClientLogger = bspClientLogger;
      this.bazelInfo = bazelInfo;
      this.bazelRunner = bazelRunner;
      this.compilationManager = compilationManager;
      this.languagePluginsService = languagePluginsService;
    }
  }
}
