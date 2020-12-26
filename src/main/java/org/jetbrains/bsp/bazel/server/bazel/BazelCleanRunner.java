package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.ImmutableList;

public class BazelCleanRunner {

  private static final String BAZEL_CLEAN_COMMAND = "clean";

  private final BazelRunner bazelRunner;

  public BazelCleanRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ProcessResults clean() {
    return bazelRunner.runBazelCommand(BAZEL_CLEAN_COMMAND, ImmutableList.of(), ImmutableList.of());
  }
}
