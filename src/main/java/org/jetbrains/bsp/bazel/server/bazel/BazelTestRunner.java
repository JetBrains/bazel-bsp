package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.utils.BazelArgumentsUtils;

public class BazelTestRunner {

  private static final String BAZEL_TEST_COMMAND = "test";

  private final BazelRunner bazelRunner;

  public BazelTestRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ProcessResults test(List<String> targets, List<String> arguments) {
    String joinedBazelTargets = BazelArgumentsUtils.getJoinedBazelTargets(targets);
    List<String> bazelArguments = getBazelArguments(joinedBazelTargets, arguments);

    return bazelRunner.runBazelCommand(BAZEL_TEST_COMMAND, ImmutableList.of(), bazelArguments);
  }


  private List<String> getBazelArguments(String targets, List<String> arguments) {
    List<String> bazelArguments = new ArrayList<>();
    bazelArguments.add(targets);
    bazelArguments.addAll(arguments);

    return bazelArguments;
  }
}
