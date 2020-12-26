package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.utils.BazelArgumentsUtils;

public class BazelQueryRunner {

  private static final String BAZEL_QUERY_COMMAND = "query";
  private static final String BAZEL_OUTPUT_PROTO_FLAG = "--output=proto";

  private final BazelRunner bazelRunner;

  public BazelQueryRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ProcessResults queryWithOutputProto(List<String> targets) {
    List<String> flags = ImmutableList.of(BAZEL_OUTPUT_PROTO_FLAG);
    List<String> arguments = ImmutableList.of(BazelArgumentsUtils.getJoinedBazelTargets(targets));

    return query(flags, arguments);
  }

  public ProcessResults queryWithOutputProto(String argument) {
    List<String> flags = ImmutableList.of(BAZEL_OUTPUT_PROTO_FLAG);
    List<String> arguments = ImmutableList.of(argument);

    return query(flags, arguments);
  }

  public ProcessResults query(List<String> flags, List<String> arguments) {
    return bazelRunner.runBazelCommand(BAZEL_QUERY_COMMAND, flags, arguments);
  }
}
