package org.jetbrains.bsp.bazel.server.bsp.resolvers.targets;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;

public class TargetsResolver {

  private final BazelData bazelData;
  private final BazelRunner bazelRunner;
  private final String compilerOptionsName;

  public TargetsResolver(BazelData bazelData, BazelRunner bazelRunner, String compilerOptionsName) {
    this.bazelData = bazelData;
    this.bazelRunner = bazelRunner;
    this.compilerOptionsName = compilerOptionsName;
  }

  public Map<String, List<String>> getTargetsOptions(List<String> targets) {
    BazelProcessResult bazelProcessResult =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withTargets(targets)
            .executeBazelCommand();
    Build.QueryResult query = QueryResolver.getQueryResultForProcess(bazelProcessResult);

    return query.getTargetList().stream()
        .map(Build.Target::getRule)
        .collect(
            Collectors.toMap(
                Build.Rule::getName,
                (rule) ->
                    rule.getAttributeList().stream()
                        .filter(attr -> attr.getName().equals(compilerOptionsName))
                        .flatMap(attr -> attr.getStringListValueList().stream())
                        .collect(Collectors.toList())));
  }
}
