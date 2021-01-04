package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.JavaBuildServer;
import ch.epfl.scala.bsp4j.ScalaBuildServer;
import io.grpc.ServerBuilder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.server.bazel.BazelDataResolver;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerBuildManager;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerConfig;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerRequestHelpers;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.impl.BuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.impl.JavaBuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.impl.ScalaBuildServerImpl;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.ActionGraphResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsResolver;
import org.jetbrains.bsp.bazel.server.bsp.services.BuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.JavaBuildServerService;
import org.jetbrains.bsp.bazel.server.bsp.services.ScalaBuildServerService;
import org.jetbrains.bsp.bazel.server.logger.BuildClientLogger;

public class BazelBspServer {

  private final BazelBspServerConfig serverConfig;
  private final BazelRunner bazelRunner;
  private final BazelData bazelData;

  private BuildServer buildServer;
  private JavaBuildServer javaBuildServer;
  private ScalaBuildServer scalaBuildServer;

  private BazelBspServerBuildManager serverBuildManager;

  public BazelBspServer(BazelBspServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    this.bazelRunner = new BazelRunner(serverConfig.getBazelPath());
    BazelDataResolver bazelDataResolver = new BazelDataResolver(bazelRunner);
    this.bazelData = bazelDataResolver.resolveBazelData();
  }

  public void startServer(BspIntegrationData bspIntegrationData) {
    BazelBspServerLifetime serverLifetime = new BazelBspServerLifetime();
    BazelBspServerRequestHelpers serverRequestHelpers =
        new BazelBspServerRequestHelpers(serverLifetime);

    TargetsResolver targetsResolver = new TargetsResolver(bazelRunner);
    ActionGraphResolver actionGraphResolver = new ActionGraphResolver(bazelRunner);

    this.serverBuildManager =
        new BazelBspServerBuildManager(serverConfig, serverRequestHelpers, bazelData, bazelRunner);

    BuildServerService buildServerService =
        new BuildServerService(
            serverRequestHelpers, serverLifetime, serverBuildManager, bazelData, bazelRunner);

    ScalaBuildServerService scalaBuildServerService =
        new ScalaBuildServerService(bazelData, targetsResolver, actionGraphResolver);
    JavaBuildServerService javaBuildServerService =
        new JavaBuildServerService(bazelData, targetsResolver, actionGraphResolver);

    this.scalaBuildServer = new ScalaBuildServerImpl(scalaBuildServerService, serverRequestHelpers);
    this.javaBuildServer = new JavaBuildServerImpl(javaBuildServerService, serverRequestHelpers);
    this.buildServer = new BuildServerImpl(buildServerService, serverRequestHelpers);

    integrateBsp(bspIntegrationData);
  }

  private void integrateBsp(BspIntegrationData bspIntegrationData) {
    Launcher<BuildClient> launcher =
        new Launcher.Builder<BuildClient>()
            .traceMessages(bspIntegrationData.getTraceWriter())
            .setOutput(bspIntegrationData.getStdout())
            .setInput(bspIntegrationData.getStdin())
            .setLocalService(buildServer)
            .setRemoteInterface(BuildClient.class)
            .setExecutorService(bspIntegrationData.getExecutor())
            .create();
    bspIntegrationData.setLauncher(launcher);

    BuildClientLogger buildClientLogger = new BuildClientLogger(launcher.getRemoteProxy());
    BepServer bepServer = new BepServer(bazelData, launcher.getRemoteProxy(), buildClientLogger);
    serverBuildManager.setBepServer(bepServer);

    bspIntegrationData.setServer(ServerBuilder.forPort(0).addService(bepServer).build());
  }

  public void setBesBackendPort(int port) {
    bazelRunner.setBesBackendPort(port);
  }
}
