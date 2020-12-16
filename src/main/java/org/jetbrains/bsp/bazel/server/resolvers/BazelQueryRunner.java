package org.jetbrains.bsp.bazel.server.resolvers;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import org.jetbrains.bsp.bazel.common.Constants;
import org.jetbrains.bsp.bazel.server.data.ProcessResults;

public class BazelQueryRunner {

  private static final String ALL_TARGETS_QUERY_PARAM = "//...";
  private static final String OUTPUT_PROTO_FLAG = "--output=proto";

  private final BazelRunner bazelRunner;

  public BazelQueryRunner(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public Build.QueryResult queryAllTargets() {
    return queryWithProtobufOutput(ALL_TARGETS_QUERY_PARAM);
  }

  public Build.QueryResult queryWithProtobufOutput(String command) {
    return getQuery(Constants.BAZEL_QUERY_COMMAND, OUTPUT_PROTO_FLAG, command);
  }

  public Build.QueryResult getQuery(String... args) {
    try {
      ProcessResults process = bazelRunner.runBazelCommand(args);
      return Build.QueryResult.parseFrom(process.getStdoutStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
