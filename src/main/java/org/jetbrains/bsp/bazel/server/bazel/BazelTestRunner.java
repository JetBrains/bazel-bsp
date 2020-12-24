package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

public class BazelTestRunner {

  private static final String BAZEL_TEST_COMMAND = "test";

  private final BazelRunner bazelRunner;

  public BazelTestRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ProcessResults runTest(List<String> targets, List<String> arguments) {
    String bazelTargets = getBazelTargets(targets);
    List<String> bazelArguments = getBazelArguments(bazelTargets, arguments);

    return bazelRunner.runBazelCommand(BAZEL_TEST_COMMAND, ImmutableList.of(), bazelArguments);
  }

  private String getBazelTargets(List<String> targets) {
    return "(" + Joiner.on("+").join(targets) + ")";
  }

  private List<String> getBazelArguments(String targets, List<String> arguments) {
    List<String> bazelArguments = new ArrayList<>();
    bazelArguments.add(targets);
    bazelArguments.addAll(arguments);

    return bazelArguments;
  }
}
