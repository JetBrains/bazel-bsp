package org.jetbrains.bsp.bazel.server.bazel;

import java.util.List;

public class BazelBuildRunner {

  private static final String BAZEL_BUILD_COMMAND = "build";

  private final BazelRunner bazelRunner;

  public BazelBuildRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ProcessResults build(List<String> flags, List<String> targets) {
    return bazelRunner.runBazelCommand(BAZEL_BUILD_COMMAND, flags, targets);
  }
}
