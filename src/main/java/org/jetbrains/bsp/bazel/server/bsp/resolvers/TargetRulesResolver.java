package org.jetbrains.bsp.bazel.server.bsp.resolvers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import com.google.devtools.build.lib.query2.proto.proto2api.Build.Rule;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.server.bazel.BazelProcess;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;

public class TargetRulesResolver<T> {

  private final BazelRunner bazelRunner;

  private final Predicate<Build.Rule> filter;
  private final Function<Build.Rule, T> mapper;

  private TargetRulesResolver(
      BazelRunner bazelRunner, Predicate<Build.Rule> filter, Function<Rule, T> mapper) {
    this.bazelRunner = bazelRunner;
    this.filter = filter;
    this.mapper = mapper;
  }

  public static <T> TargetRulesResolver<T> withBazelRunnerAndMapper(
      BazelRunner bazelRunner, Function<Rule, T> mapper) {
    return withBazelRunnerAndFilterAndMapper(bazelRunner, o -> true, mapper);
  }

  public static <T> TargetRulesResolver<T> withBazelRunnerAndFilterAndMapper(
      BazelRunner bazelRunner, Predicate<Build.Rule> filter, Function<Rule, T> mapper) {
    return new TargetRulesResolver<T>(bazelRunner, filter, mapper);
  }

  public List<T> getItemsForTargets(List<BuildTargetIdentifier> targetsIds) {
    Build.QueryResult queryResult = getBuildQueryResult(targetsIds);

    return queryResult.getTargetList().stream()
        .map(Build.Target::getRule)
        .filter(filter)
        .map(mapper)
        .collect(Collectors.toList());
  }

  private Build.QueryResult getBuildQueryResult(List<BuildTargetIdentifier> targetsIds) {
    List<String> targets = TargetsUtils.getTargetsUris(targetsIds);
    BazelProcess bazelProcess = queryBazel(targets);

    return QueryResolver.getQueryResultForProcess(bazelProcess);
  }

  private BazelProcess queryBazel(List<String> targets) {
    return bazelRunner
        .commandBuilder()
        .query()
        .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
        .withTargets(targets)
        .executeBazelBesCommand();
  }
}
