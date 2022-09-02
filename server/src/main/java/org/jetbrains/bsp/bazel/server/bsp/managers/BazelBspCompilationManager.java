package org.jetbrains.bsp.bazel.server.bsp.managers;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec;

public class BazelBspCompilationManager {

  private final BazelRunner bazelRunner;
  private BepServer bepServer;

  public BazelBspCompilationManager(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public BepBuildResult buildTargetsWithBep(TargetsSpec targetSpecs, String originId) {
    return buildTargetsWithBep(targetSpecs, List.empty(), originId);
  }

  public BepBuildResult buildTargetsWithBep(
      TargetsSpec targetSpecs, Seq<String> extraFlags, String originId) {
    var result =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlags(extraFlags.asJava())
            .withTargets(targetSpecs)
            .executeBazelBesCommand(originId)
            .waitAndGetResult(false);
    return new BepBuildResult(result, bepServer.getBepOutput());
  }

  public void setBepServer(BepServer bepServer) {
    this.bepServer = bepServer;
  }
}
