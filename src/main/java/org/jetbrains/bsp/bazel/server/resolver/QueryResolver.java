package org.jetbrains.bsp.bazel.server.resolver;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.BazelQueryRunner;
import org.jetbrains.bsp.bazel.server.bazel.ProcessResults;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;

public class QueryResolver {

  private final BazelQueryRunner bazelQueryRunner;

  public QueryResolver(BazelRunner bazelRunner) {
    this.bazelQueryRunner = new BazelQueryRunner(bazelRunner);
  }

  public Build.QueryResult getQuery(List<String> flags, String argument) {
    List<String> arguments = ImmutableList.of(argument);
    ProcessResults process = bazelQueryRunner.query(flags, arguments);

    return getQueryResultForProcess(process);
  }

  public Build.QueryResult getQueryResultForDep(String dep) {
    ProcessResults process = bazelQueryRunner.queryWithOutputProto(dep);

    return getQueryResultForProcess(process);
  }

  public Build.QueryResult getQueryResultForTargets(List<String> targets) {
    ProcessResults process = bazelQueryRunner.queryWithOutputProto(targets);

    return getQueryResultForProcess(process);
  }

  private Build.QueryResult getQueryResultForProcess(ProcessResults process) {
    try {
      return Build.QueryResult.parseFrom(process.getStdoutStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
