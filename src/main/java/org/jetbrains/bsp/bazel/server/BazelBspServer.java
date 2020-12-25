package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.JavaBuildServer;
import ch.epfl.scala.bsp4j.ScalaBuildServer;
import io.grpc.ServerBuilder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.server.data.BazelData;
import org.jetbrains.bsp.bazel.server.logger.BuildClientLogger;
import org.jetbrains.bsp.bazel.server.resolvers.ActionGraphResolver;
import org.jetbrains.bsp.bazel.server.resolvers.BazelDataResolver;
import org.jetbrains.bsp.bazel.server.resolvers.BazelRunner;
import org.jetbrains.bsp.bazel.server.resolvers.QueryResolver;
import org.jetbrains.bsp.bazel.server.resolvers.TargetsResolver;

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

  public void startServer(BspIntegration bspIntegration) {
    BazelBspServerLifetime serverLifetime = new BazelBspServerLifetime();
    BazelBspServerRequestHelpers serverRequestHelpers =
        new BazelBspServerRequestHelpers(serverLifetime);

    QueryResolver queryResolver = new QueryResolver(bazelRunner);
    TargetsResolver targetsResolver = new TargetsResolver(queryResolver);
    ActionGraphResolver actionGraphResolver = new ActionGraphResolver(bazelRunner);

    this.serverBuildManager =
        new BazelBspServerBuildManager(
            serverConfig, serverRequestHelpers, bazelData, bazelRunner, queryResolver);

    BuildServerService buildServerService =
        new BuildServerService(
            serverRequestHelpers,
            serverLifetime,
            serverBuildManager,
            bazelData,
            bazelRunner,
            queryResolver);
    ScalaBuildServerService scalaBuildServerService =
        new ScalaBuildServerService(bazelData, targetsResolver, actionGraphResolver);
    JavaBuildServerService javaBuildServerService =
        new JavaBuildServerService(bazelData, targetsResolver, actionGraphResolver);

    this.scalaBuildServer = new ScalaBuildServerImpl(scalaBuildServerService, serverRequestHelpers);
    this.javaBuildServer = new JavaBuildServerImpl(javaBuildServerService, serverRequestHelpers);
    this.buildServer = new BuildServerImpl(buildServerService, serverRequestHelpers);

    integrateBsp(bspIntegration);
  }

  private void integrateBsp(BspIntegration bspIntegration) {
    Launcher<BuildClient> launcher =
        new Launcher.Builder<BuildClient>()
            .traceMessages(bspIntegration.getTraceWriter())
            .setOutput(bspIntegration.getStdout())
            .setInput(bspIntegration.getStdin())
            .setLocalService(this.buildServer)
            .setRemoteInterface(BuildClient.class)
            .setExecutorService(bspIntegration.getExecutor())
            .create();
    bspIntegration.setLauncher(launcher);

    BuildClientLogger buildClientLogger = new BuildClientLogger(launcher.getRemoteProxy());
    BepServer bepServer = new BepServer(bazelData, launcher.getRemoteProxy(), buildClientLogger);
    serverBuildManager.setBepServer(bepServer);

    bspIntegration.setServer(ServerBuilder.forPort(0).addService(bepServer).build());
  }

  public void setBesBackendPort(int port) {
    bazelRunner.setBesBackendPort(port);
  }
}
