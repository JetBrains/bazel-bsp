package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.ImmutableList;

public class BazelInfoRunner {

  private static final String BAZEL_INFO_COMMAND = "info";

  private final BazelRunner bazelRunner;

  public BazelInfoRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ProcessResults info(String argument) {
    return bazelRunner.runBazelCommand(BAZEL_INFO_COMMAND, ImmutableList.of(), ImmutableList.of(argument));
  }
}
