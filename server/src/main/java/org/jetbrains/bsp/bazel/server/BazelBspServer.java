package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.CppBuildServer;
import ch.epfl.scala.bsp4j.JavaBuildServer;
import ch.epfl.scala.bsp4j.JvmBuildServer;
import ch.epfl.scala.bsp4j.ScalaBuildServer;
import io.grpc.ServerBuilder;
import java.nio.file.Paths;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.bazelrunner.BazelDataResolver;
import org.jetbrains.bsp.bazel.bazelrunner.BazelDataResolver.BazelDataResolverException;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerBuildManager;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerRequestHelpers;
import org.jetbrains.bsp.bazel.server.bsp.BspImplementationHub;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.config.BazelBspServerConfig;
import org.jetbrains.bsp.bazel.server.bsp.impl.BuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.impl.CppBuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.impl.JavaBuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.impl.JvmBuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.impl.ScalaBuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspQueryManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspTargetManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelCppTargetManager;
import org.jetbrains.bsp.bazel.server.bsp.services.BuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.CppBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.JavaBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.JvmBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.ScalaBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver;
import org.jetbrains.bsp.bazel.server.loggers.BuildClientLogger;

public class BazelBspServer {

  private final BazelBspServerConfig bazelBspServerConfig;
  private final BazelRunner bazelRunner;

  private BazelData bazelData;
  private BspImplementationHub bspImplementationHub;
  private BazelBspServerBuildManager serverBuildManager;

  public BazelBspServer(BazelBspServerConfig bazelBspServerConfig) {
    this.bazelBspServerConfig = bazelBspServerConfig;
    this.bazelRunner = new BazelRunner(bazelBspServerConfig.getBazelPath());
  }

  public void startServer(BspIntegrationData bspIntegrationData) {
    BazelBspServerLifetime serverLifetime = new BazelBspServerLifetime();
    BazelBspServerRequestHelpers serverRequestHelpers =
        new BazelBspServerRequestHelpers(serverLifetime);

    setupLauncher(bspIntegrationData);

    BuildClientLogger buildClientLogger = bspIntegrationData.getBuildClientLogger();

    resolveBazelData(buildClientLogger);

    BazelBspCompilationManager bazelBspCompilationManager =
        new BazelBspCompilationManager(bazelRunner, bazelData, buildClientLogger);
    InternalAspectsResolver internalAspectsResolver =
        new InternalAspectsResolver(
            bazelData.getBspProjectRoot(), Paths.get(bazelData.getWorkspaceRoot()));
    BazelBspAspectsManager bazelBspAspectsManager =
        new BazelBspAspectsManager(
            bazelBspCompilationManager, bazelRunner, internalAspectsResolver);
    BazelCppTargetManager bazelCppTargetManager = new BazelCppTargetManager(bazelBspAspectsManager);
    BazelBspTargetManager bazelBspTargetManager =
        new BazelBspTargetManager(
            bazelRunner, bazelData, bazelBspAspectsManager, bazelCppTargetManager);
    BazelBspQueryManager bazelBspQueryManager =
        new BazelBspQueryManager(
            bazelBspServerConfig.getProjectView(),
            bazelData,
            bazelRunner,
            bazelBspTargetManager,
            buildClientLogger);

    this.serverBuildManager =
        new BazelBspServerBuildManager(
            serverRequestHelpers,
            bazelBspCompilationManager,
            bazelBspAspectsManager,
            bazelBspTargetManager,
            bazelCppTargetManager,
            bazelBspQueryManager);

    BuildServerService buildServerService =
        new BuildServerService(
            serverRequestHelpers,
            serverLifetime,
            serverBuildManager,
            bazelData,
            bazelRunner,
            buildClientLogger,
            bazelBspServerConfig.getProjectView());

    JvmBuildServerService jvmBuildServerService =
        new JvmBuildServerService(bazelData, bazelRunner, bazelBspAspectsManager);
    ScalaBuildServerService scalaBuildServerService =
        new ScalaBuildServerService(
            bazelData, bazelRunner, bazelBspCompilationManager, bazelBspQueryManager);
    JavaBuildServerService javaBuildServerService =
        new JavaBuildServerService(
            bazelBspCompilationManager, bazelBspQueryManager, bazelData, bazelRunner);
    CppBuildServerService cppBuildServerService =
        new CppBuildServerService(bazelRunner, bazelBspAspectsManager);

    JvmBuildServer jvmBuildServer =
        new JvmBuildServerImpl(jvmBuildServerService, serverRequestHelpers);
    ScalaBuildServer scalaBuildServer =
        new ScalaBuildServerImpl(scalaBuildServerService, serverRequestHelpers);
    JavaBuildServer javaBuildServer =
        new JavaBuildServerImpl(javaBuildServerService, serverRequestHelpers);
    CppBuildServer cppBuildServer =
        new CppBuildServerImpl(cppBuildServerService, serverRequestHelpers);
    BuildServer buildServer = new BuildServerImpl(buildServerService, serverRequestHelpers);

    this.bspImplementationHub =
        new BspImplementationHub(
            buildServer, jvmBuildServer, scalaBuildServer, javaBuildServer, cppBuildServer);

    integrateBsp(bspIntegrationData);
  }

  private void setupLauncher(BspIntegrationData bspIntegrationData) {
    Launcher<BuildClient> launcher =
        new Launcher.Builder<BuildClient>()
            .traceMessages(bspIntegrationData.getTraceWriter())
            .setOutput(bspIntegrationData.getStdout())
            .setInput(bspIntegrationData.getStdin())
            .setLocalService(bspImplementationHub)
            .setRemoteInterface(BuildClient.class)
            .setExecutorService(bspIntegrationData.getExecutor())
            .create();
    bspIntegrationData.setLauncher(launcher);
    BuildClientLogger buildClientLogger = new BuildClientLogger(launcher.getRemoteProxy());
    bspIntegrationData.setBuildClientLogger(buildClientLogger);
  }

  private void resolveBazelData(BuildClientLogger buildClientLogger) {
    BazelDataResolver bazelDataResolver = new BazelDataResolver(bazelRunner);
    try {
      this.bazelData = bazelDataResolver.resolveBazelData();
    } catch (BazelDataResolverException e) {
      buildClientLogger.logError(e.getMessage());
    }
  }

  private void integrateBsp(BspIntegrationData bspIntegrationData) {
    BuildClient buildClient = bspIntegrationData.getLauncher().getRemoteProxy();
    BuildClientLogger buildClientLogger = bspIntegrationData.getBuildClientLogger();
    BepServer bepServer = new BepServer(bazelData, buildClient, buildClientLogger);
    serverBuildManager.setBepServer(bepServer);

    bspIntegrationData.setServer(ServerBuilder.forPort(0).addService(bepServer).build());
  }

  public void setBesBackendPort(int port) {
    bazelRunner.setBesBackendPort(port);
  }
}
