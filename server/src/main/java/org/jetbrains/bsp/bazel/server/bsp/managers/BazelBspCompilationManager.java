package org.jetbrains.bsp.bazel.server.bsp.managers;

import io.vavr.collection.List;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec;

public class BazelBspCompilationManager {

  private BepServer bepServer;
  private final BazelRunner bazelRunner;

  public BazelBspCompilationManager(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public BepBuildResult buildTargetsWithBep(TargetsSpec targetSpecs) {
    return buildTargetsWithBep(targetSpecs, List.empty());
  }

  public BepBuildResult buildTargetsWithBep(TargetsSpec targetSpecs, List<String> extraFlags) {
    var result =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlags(extraFlags.asJava())
            .withTargets(targetSpecs)
            .executeBazelBesCommand()
            .waitAndGetResult();
    return new BepBuildResult(result, bepServer.getBepOutput());
  }

  public void setBepServer(BepServer bepServer) {
    this.bepServer = bepServer;
  }
}
