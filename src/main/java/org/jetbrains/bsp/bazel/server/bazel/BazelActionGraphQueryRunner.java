package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.utils.BazelArgumentsUtils;

public class BazelActionGraphQueryRunner {

  private static final String BAZEL_AQUERY_COMMAND = "aquery";
  private static final String BAZEL_OUTPUT_PROTO_FLAG = "--output=proto";

  private final BazelRunner bazelRunner;

  public BazelActionGraphQueryRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ProcessResults aquery(List<String> targets, List<String> languageIds) {
    List<String> flags = ImmutableList.of(BAZEL_OUTPUT_PROTO_FLAG);
    List<String> arguments = ImmutableList.of(
        BazelArgumentsUtils.getMnemonicWithJoinedTargets(targets, languageIds));

    return bazelRunner.runBazelCommand(BAZEL_AQUERY_COMMAND, flags, arguments);
  }
}
