package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

public class BazelRunRunner {

  private static final String BAZEL_RUN_COMMAND = "run";

  private final BazelRunner bazelRunner;

  public BazelRunRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ProcessResults run(String target, List<String> arguments) {
    List<String> bazelArguments = getBazelArguments(target, arguments);

    return bazelRunner.runBazelCommand(BAZEL_RUN_COMMAND, ImmutableList.of(), bazelArguments);
  }

  private List<String> getBazelArguments(String target, List<String> arguments) {
    List<String> bazelArguments = new ArrayList<>();
    bazelArguments.add(target);
    bazelArguments.addAll(arguments);

    return bazelArguments;
  }
}
