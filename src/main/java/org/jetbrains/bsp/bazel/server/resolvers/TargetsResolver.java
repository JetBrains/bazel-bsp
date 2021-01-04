package org.jetbrains.bsp.bazel.server.resolvers;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import java.util.stream.Stream;

public class TargetsResolver {

  private final BazelRunner bazelRunner;

  private static final String MAIN_CLASS_ATTR_NAME = "main_class";

  public TargetsResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public Map<String, List<String>> getTargetsOptions(
      List<String> targets, String compilerOptionsName) {
    return queryTargets(targets).getTargetList().stream()
        .map(Build.Target::getRule)
        .collect(
            Collectors.toMap(
                Build.Rule::getName,
                (rule) ->
                    getAttribute(rule, compilerOptionsName)
                        .flatMap(attr -> attr.getStringListValueList().stream())
                        .collect(Collectors.toList())));
  }

  public Map<String, List<String>> getTargetsMainClasses(List<String> targets) {
    return queryTargets(targets).getTargetList().stream()
        .map(Build.Target::getRule)
        .collect(
            Collectors.toMap(
                Build.Rule::getName,
                (rule) ->
                    getAttribute(rule, MAIN_CLASS_ATTR_NAME)
                        .map(Build.Attribute::getStringValue)
                        .collect(Collectors.toList())));
  }

  private Stream<Build.Attribute> getAttribute(Build.Rule rule, String name) {
    return rule.getAttributeList().stream()
        .filter(
            attr ->
                attr.getName().equals(name)
                    && attr.hasExplicitlySpecified()
                    && attr.getExplicitlySpecified());
  }

  private Build.QueryResult queryTargets(List<String> targets) {
    BazelProcessResult bazelProcessResult =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withTargets(targets)
            .executeBazelCommand();
    return QueryResolver.getQueryResultForProcess(bazelProcessResult);
  }
}
