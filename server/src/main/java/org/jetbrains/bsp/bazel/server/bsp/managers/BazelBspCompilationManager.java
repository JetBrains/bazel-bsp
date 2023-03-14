package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildClient;
import io.grpc.ServerBuilder;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec;

public class BazelBspCompilationManager {

  private final BazelRunner bazelRunner;
  private BuildClient client;
  private Path workspaceRoot;

  public BazelBspCompilationManager(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
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
    var bepServer = new BepServer(client, new DiagnosticsService(workspaceRoot));
    var server = ServerBuilder.forPort(0).addService(bepServer).build();
    try {
      server.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    bazelRunner.setBesBackendPort(server.getPort());
    try {
      var result =
          bazelRunner
              .commandBuilder()
              .build()
              .withFlags(extraFlags.asJava())
              .withTargets(targetSpecs)
              .executeBazelBesCommand(originId)
              .waitAndGetResult(cancelChecker, true);
      return new BepBuildResult(result, bepServer.getBepOutput());
    } finally {
      server.shutdown();
    }
  }

  public void setClient(BuildClient client) {
    this.client = client;
  }

  public void setWorkspaceRoot(Path workspaceRoot) {
    this.workspaceRoot = workspaceRoot;
  }
}
