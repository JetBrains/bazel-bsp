package org.jetbrains.bsp.bazel.server.resolver;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import org.jetbrains.bsp.bazel.server.data.ProcessResults;

public class QueryResolver {

  private final BazelRunner bazelRunner;

  public QueryResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
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
