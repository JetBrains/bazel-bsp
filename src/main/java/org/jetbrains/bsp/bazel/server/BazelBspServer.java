package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.CppBuildServer;
import ch.epfl.scala.bsp4j.JavaBuildServer;
import ch.epfl.scala.bsp4j.ScalaBuildServer;
import io.grpc.ServerBuilder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.server.bazel.BazelDataResolver;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
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
import org.jetbrains.bsp.bazel.server.bsp.impl.ScalaBuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspQueryManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspTargetManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelCppTargetManager;
import org.jetbrains.bsp.bazel.server.bsp.services.BuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.CppBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.JavaBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.ScalaBuildServerService;
import org.jetbrains.bsp.bazel.server.loggers.BuildClientLogger;

public class BazelBspServer {

  private final BazelBspServerConfig bazelBspServerConfig;
  private final BazelRunner bazelRunner;
  private final BazelData bazelData;

  private BspImplementationHub bspImplementationHub;
  private BazelBspServerBuildManager serverBuildManager;

  public BazelBspServer(BazelBspServerConfig bazelBspServerConfig) {
    this.bazelBspServerConfig = bazelBspServerConfig;
    this.bazelRunner = new BazelRunner(bazelBspServerConfig.getBazelPath());
    BazelDataResolver bazelDataResolver = new BazelDataResolver(bazelRunner);
    this.bazelData = bazelDataResolver.resolveBazelData();
  }

  public void startServer(BspIntegrationData bspIntegrationData) {
    BazelBspServerLifetime serverLifetime = new BazelBspServerLifetime();
    BazelBspServerRequestHelpers serverRequestHelpers =
        new BazelBspServerRequestHelpers(serverLifetime);

    BazelBspCompilationManager bazelBspCompilationManager =
        new BazelBspCompilationManager(bazelRunner, bazelData);
    BazelBspAspectsManager bazelBspAspectsManager =
        new BazelBspAspectsManager(bazelBspCompilationManager, bazelRunner);
    BazelCppTargetManager bazelCppTargetManager = new BazelCppTargetManager(bazelBspAspectsManager);
    BazelBspTargetManager bazelBspTargetManager =
        new BazelBspTargetManager(bazelRunner, bazelBspAspectsManager, bazelCppTargetManager);
    BazelBspQueryManager bazelBspQueryManager =
        new BazelBspQueryManager(
            bazelBspServerConfig.getProjectView(), bazelData, bazelRunner, bazelBspTargetManager);

    this.serverBuildManager =
        new BazelBspServerBuildManager(
            serverRequestHelpers,
            bazelData,
            bazelRunner,
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
            bazelBspServerConfig.getProjectView());

    ScalaBuildServerService scalaBuildServerService =
        new ScalaBuildServerService(
            bazelData, bazelRunner, bazelBspCompilationManager, bazelBspQueryManager);
    JavaBuildServerService javaBuildServerService =
        new JavaBuildServerService(
            bazelBspCompilationManager, bazelBspQueryManager, bazelData, bazelRunner);
    CppBuildServerService cppBuildServerService = new CppBuildServerService(bazelRunner);

    ScalaBuildServer scalaBuildServer =
        new ScalaBuildServerImpl(scalaBuildServerService, serverRequestHelpers);
    JavaBuildServer javaBuildServer =
        new JavaBuildServerImpl(javaBuildServerService, serverRequestHelpers);
    CppBuildServer cppBuildServer =
        new CppBuildServerImpl(cppBuildServerService, serverRequestHelpers);
    BuildServer buildServer = new BuildServerImpl(buildServerService, serverRequestHelpers);

    this.bspImplementationHub =
        new BspImplementationHub(buildServer, scalaBuildServer, javaBuildServer, cppBuildServer);

    integrateBsp(bspIntegrationData);
  }

  private void integrateBsp(BspIntegrationData bspIntegrationData) {
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

    BepServer bepServer = new BepServer(bazelData, launcher.getRemoteProxy(), buildClientLogger);
    serverBuildManager.setBepServer(bepServer);
    bazelRunner.setLogger(buildClientLogger);

    bspIntegrationData.setServer(ServerBuilder.forPort(0).addService(bepServer).build());
  }

  public void setBesBackendPort(int port) {
    bazelRunner.setBesBackendPort(port);
  }
}
