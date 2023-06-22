package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec;

public class BazelBspCompilationManager {

  private final BazelRunner bazelRunner;
  private BuildClient client;
  private Path workspaceRoot;
  private Map<String, Set<TextDocumentIdentifier>> hasAnyProblems;

  public BazelBspCompilationManager(
      BazelRunner bazelRunner, Map<String, Set<TextDocumentIdentifier>> hasAnyProblems) {
    this.bazelRunner = bazelRunner;
    this.hasAnyProblems = hasAnyProblems;
  }

  public BepBuildResult buildTargetsWithBep(
      CancelChecker cancelChecker, TargetsSpec targetSpecs, String originId) {
    return buildTargetsWithBep(cancelChecker, targetSpecs, List.empty(), originId);
  }

  public BepBuildResult buildTargetsWithBep(
      CancelChecker cancelChecker,
      TargetsSpec targetSpecs,
      Seq<String> extraFlags,
      String originId) {
    var bepServer = BepServer.newBepServer(client, workspaceRoot, hasAnyProblems);
    var executor = Executors.newFixedThreadPool(4, threadFactory());
    var nettyServer =
        BepServer.nettyServerBuilder().addService(bepServer).executor(executor).build();
    try {
      nettyServer.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try {
      var result =
          bazelRunner
              .commandBuilder()
              .build()
              .withFlags(extraFlags.asJava())
              .withTargets(targetSpecs)
              .executeBazelBesCommand(originId, nettyServer.getPort())
              .waitAndGetResult(cancelChecker, true);
      return new BepBuildResult(result, bepServer.getBepOutput());
    } finally {
      nettyServer.shutdown();
      executor.shutdown();
    }
  }

  private static ThreadFactory threadFactory() {
    return new ThreadFactoryBuilder()
        .setNameFormat("grpc-netty-pool-%d")
        .setUncaughtExceptionHandler(
            (t, e) -> {
              e.printStackTrace();
              System.exit(1);
            })
        .build();
  }

  public void setClient(BuildClient client) {
    this.client = client;
  }

  public BuildClient getClient() {
    return client;
  }

  public void setWorkspaceRoot(Path workspaceRoot) {
    this.workspaceRoot = workspaceRoot;
  }

  public Path getWorkspaceRoot() {
    return workspaceRoot;
  }
}
