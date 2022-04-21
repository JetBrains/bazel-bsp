package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import io.grpc.ServerBuilder;
import io.vavr.control.Option;
import java.util.List;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoResolver;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoStorage;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection;
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
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.BazelProjectMapper;
import org.jetbrains.bsp.bazel.server.sync.BspProjectMapper;
import org.jetbrains.bsp.bazel.server.sync.ExecuteService;
import org.jetbrains.bsp.bazel.server.sync.ProjectProvider;
import org.jetbrains.bsp.bazel.server.sync.ProjectResolver;
import org.jetbrains.bsp.bazel.server.sync.ProjectStorage;
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService;
import org.jetbrains.bsp.bazel.server.sync.TargetKindResolver;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService;
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin;

public class BazelBspServer {

  private final BazelRunner bazelRunner;
  private final BazelInfo bazelInfo;

  private final BspServerApi bspServerApi;
  private final BazelBspCompilationManager compilationManager;
  private final BspClientLogger bspClientLogger;

  public BazelBspServer(BazelBspServerConfig config) {
    this.bspClientLogger = new BspClientLogger();
    var bspInfo = new BspInfo();
    var bazelInfoStorage = new BazelInfoStorage(bspInfo);
    var bazelDataResolver =
        new BazelInfoResolver(BazelRunner.inCwd(config, bspClientLogger), bazelInfoStorage);
    this.bazelInfo = bazelDataResolver.resolveBazelInfo();
    this.bazelRunner =
        BazelRunner.of(
            config, bspClientLogger, bazelInfo, getDefaultBazelFlags(config.currentProjectView()));
    var serverLifetime = new BazelBspServerLifetime();
    var bspRequestsRunner = new BspRequestsRunner(serverLifetime);
    this.compilationManager = new BazelBspCompilationManager(bazelRunner);
    var aspectsResolver = new InternalAspectsResolver(bazelInfo, bspInfo);
    var bazelBspAspectsManager = new BazelBspAspectsManager(compilationManager, aspectsResolver);
    var bazelPathsResolver = new BazelPathsResolver(bazelInfo);
    var javaLanguagePlugin = new JavaLanguagePlugin(bazelPathsResolver, bazelInfo);
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
        new ProjectResolver(bazelBspAspectsManager, config, bazelProjectMapper, bspClientLogger);
    var projectStorage = new ProjectStorage(bspInfo, bspClientLogger);
    var projectProvider = new ProjectProvider(projectResolver, projectStorage);
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
  }

  // this is only a temporary solution - will be changed later
  private List<String> getDefaultBazelFlags(ProjectView projectView) {
    return Option.of(projectView.getBuildFlags())
        .map(ProjectViewBuildFlagsSection::getValues)
        .map(io.vavr.collection.List::toJavaList)
        .getOrElse(List.of());
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
    var bepServer = new BepServer(client, bspClientLogger, new DiagnosticsService(bazelInfo));
    compilationManager.setBepServer(bepServer);
    bspIntegrationData.setServer(ServerBuilder.forPort(0).addService(bepServer).build());
  }

  public void setBesBackendPort(int port) {
    bazelRunner.setBesBackendPort(port);
  }
}
