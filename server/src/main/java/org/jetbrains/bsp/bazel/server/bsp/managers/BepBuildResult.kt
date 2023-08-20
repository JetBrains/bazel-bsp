package org.jetbrains.bsp.bazel.server.bsp.managers;

import org.jetbrains.bsp.bazel.bazelrunner.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.bep.BepOutput;

public class BepBuildResult {
  private final BazelProcessResult processResult;
  private final BepOutput bepOutput;

  public BepBuildResult(BazelProcessResult processResult, BepOutput bepOutput) {
    this.processResult = processResult;
    this.bepOutput = bepOutput;
  }

  public BazelProcessResult processResult() {
    return processResult;
  }

  public BepOutput bepOutput() {
    return bepOutput;
  }
}
