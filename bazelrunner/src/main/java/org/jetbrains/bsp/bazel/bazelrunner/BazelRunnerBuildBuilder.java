package org.jetbrains.bsp.bazel.bazelrunner;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;

public class BazelRunnerBuildBuilder extends BazelRunnerBuilder {

  private static final String EXCLUDABLE_TARGETS_LIST_PREFIX = "--";

  public BazelRunnerBuildBuilder(BazelRunner bazelRunner, String bazelBuildCommand) {
    super(bazelRunner, bazelBuildCommand);
  }

  @Override
  public BazelRunnerBuilder withTargets(List<String> bazelTargets) {
    return withArguments(bazelTargets);
  }

  @Override
  public BazelRunnerBuilder withTargets(
      List<BuildTargetIdentifier> includedTargets, List<BuildTargetIdentifier> excludedTargets) {
    var arguments = Lists.newArrayList(EXCLUDABLE_TARGETS_LIST_PREFIX);
    arguments.addAll(toRawUris(includedTargets));
    arguments.addAll(calculateExcludedTargetsWithExcludedPrefix(excludedTargets));

    return withArguments(arguments);
  }

  private List<String> toRawUris(List<BuildTargetIdentifier> targets) {
    return targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.toList());
  }

  private List<String> calculateExcludedTargetsWithExcludedPrefix(
      List<BuildTargetIdentifier> targets) {
    return targets.stream()
        .map(BuildTargetIdentifier::getUri)
        .map(target -> "-" + target)
        .collect(Collectors.toList());
  }
}
