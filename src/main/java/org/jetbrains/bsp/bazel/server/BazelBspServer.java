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

// TODO better names after splitting
public class BazelBspServer {

  // TODO tidy up the attributes
  private final BazelBspServerConfig serverConfig;
  private final BazelBspServerLifetime serverLifetime;
  private final BazelRunner bazelRunner;
  private final QueryResolver queryResolver;
  private final TargetsResolver targetsResolver;
  private final ActionGraphResolver actionGraphResolver;
  private final BazelDataResolver bazelDataResolver;
  private final BazelData bazelData;
  private final BuildServer buildServer;
  private final ScalaBuildServer scalaBuildServer;
  private final JavaBuildServer javaBuildServer;
  private final BazelBspServerBuildManager serverBuildManager;
  private final BazelBspServerRequestHelpers serverRequestHelpers;
  private BepServer bepServer = null;
  // TODO: created in setter `setBuildClient`, HAS TO BE moved to the constructor
  private BuildClientLogger buildClientLogger;

  // TODO: imho bsp server creation on the server side is too ambiguous
  // (constructor + setters)
  public BazelBspServer(BazelBspServerConfig serverConfig, BspIntegration bspIntegration) {
    this.serverConfig = serverConfig;
    this.serverLifetime = new BazelBspServerLifetime();
    this.serverRequestHelpers = new BazelBspServerRequestHelpers(serverLifetime);

    this.bazelRunner = new BazelRunner(serverConfig.getBazelPath());
    this.queryResolver = new QueryResolver(bazelRunner);
    this.targetsResolver = new TargetsResolver(queryResolver);
    this.actionGraphResolver = new ActionGraphResolver(bazelRunner);
    this.bazelDataResolver = new BazelDataResolver(bazelRunner);
    this.bazelData = bazelDataResolver.resolveBazelData();

    this.scalaBuildServer =
        new ScalaBuildServerImpl(
            serverRequestHelpers, bazelData, targetsResolver, actionGraphResolver);
    this.javaBuildServer =
        new JavaBuildServerImpl(
            serverRequestHelpers, bazelData, targetsResolver, actionGraphResolver);

    // TODO problem - still a circular dependency
    this.serverBuildManager =
        new BazelBspServerBuildManager(
            bazelData, bazelRunner, null, null, queryResolver);

    this.buildServer =
        new BuildServerImpl(serverConfig, serverLifetime, serverRequestHelpers, serverBuildManager, bazelData, bazelRunner, queryResolver);
    serverBuildManager.setBuildServer(buildServer);

    integrateBsp(bspIntegration);
  }

  private void integrateBsp(BspIntegration bspIntegration) {
    Launcher<BuildClient> launcher = new Launcher.Builder<BuildClient>()
            .traceMessages(bspIntegration.getTraceWriter())
            .setOutput(bspIntegration.getStdout())
            .setInput(bspIntegration.getStdin())
            .setLocalService(buildServer)
            .setRemoteInterface(BuildClient.class)
            .setExecutorService(bspIntegration.getExecutor())
            .create();
    bspIntegration.setLauncher(launcher);

    this.buildClientLogger = new BuildClientLogger(launcher.getRemoteProxy());
    this.bepServer = new BepServer(this, launcher.getRemoteProxy(), buildClientLogger);
    serverBuildManager.setBepServer(bepServer);

    bspIntegration.setServer(ServerBuilder.forPort(0).addService(bepServer).build());
  }

  public void setBesBackendPort(int port) {
    bazelRunner.setBesBackendPort(port);
  }

  // TODO Remove after the dependency between BspServer and BepServer is made more sensible.
  // Only used in BepServer because of the problems with circular dependency between it
  // and this class.
  // BazelData and BazelBspServerBuildManager should be added as dependencies in the constructor
  @Deprecated
  public BazelData getBazelData() {
    return bazelData;
  }

  @Deprecated
  public BazelBspServerBuildManager getServerBuildManager() {
    return serverBuildManager;
  }
}
