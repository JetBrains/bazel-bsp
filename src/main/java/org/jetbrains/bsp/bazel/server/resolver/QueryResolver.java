package org.jetbrains.bsp.bazel.server.resolver;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bazel.ProcessResults;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;

public class QueryResolver {

  private final BazelRunner bazelRunner;

  public QueryResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public Build.QueryResult getQuery(List<String> flags, String argument) {
    ProcessResults process =
        bazelRunner.commandBuilder()
            .query()
            .withFlags(flags)
            .withArgument(argument)
            .runBazel();

    return getQueryResultForProcess(process);
  }

  public Build.QueryResult getQueryResultForDep(String dep) {
    ProcessResults process =
        bazelRunner.commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withArgument(dep)
            .runBazel();

    return getQueryResultForProcess(process);
  }

  public Build.QueryResult getQueryResultForTargets(List<String> targets) {
    ProcessResults process =
        bazelRunner.commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withArguments(targets)
            .runBazel();

    return getQueryResultForProcess(process);
  }

  public Build.QueryResult getQueryResultForProcess(ProcessResults process) {
    try {
      return Build.QueryResult.parseFrom(process.getStdoutStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
