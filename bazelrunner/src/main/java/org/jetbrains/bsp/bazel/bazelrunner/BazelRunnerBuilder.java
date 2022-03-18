package org.jetbrains.bsp.bazel.bazelrunner;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelQueryKindParameters;
import org.jetbrains.bsp.bazel.bazelrunner.utils.BazelArgumentsUtils;

public class BazelRunnerBuilder {

  private final BazelRunner bazelRunner;
  private final String bazelCommand;

  private final List<String> flags;
  private final List<String> arguments;

  BazelRunnerBuilder(BazelRunner bazelRunner, String bazelCommand) {
    this.bazelRunner = bazelRunner;
    this.bazelCommand = bazelCommand;
    this.flags = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  public BazelRunnerBuilder withFlag(String bazelFlag) {
    flags.add(bazelFlag);

    return this;
  }

  public BazelRunnerBuilder withFlags(List<String> bazelFlags) {
    flags.addAll(bazelFlags);

    return this;
  }

  public BazelRunnerBuilder withArgument(String bazelArgument) {
    arguments.add(bazelArgument);

    return this;
  }

  public BazelRunnerBuilder withArguments(List<String> bazelArguments) {
    arguments.addAll(bazelArguments);

    return this;
  }

  public BazelRunnerBuilder withTargets(List<String> bazelTargets) {
    String joinedTargets = BazelArgumentsUtils.getJoinedBazelTargets(bazelTargets);
    arguments.add(joinedTargets);

    return this;
  }

  public BazelRunnerBuilder withTargets(
      List<BuildTargetIdentifier> includedTargets, List<BuildTargetIdentifier> excludedTargets) {
    var joinedTargets = BazelArgumentsUtils.joinBazelTargets(includedTargets, excludedTargets);
    arguments.add(joinedTargets);

    return this;
  }

  public BazelRunnerBuilder withMnemonic(List<String> bazelTargets, List<String> languageIds) {
    String argument = BazelArgumentsUtils.getMnemonicWithJoinedTargets(bazelTargets, languageIds);
    arguments.add(argument);

    return this;
  }

  public BazelRunnerBuilder withKind(BazelQueryKindParameters bazelParameter) {
    return withKinds(ImmutableList.of(bazelParameter));
  }

  public BazelRunnerBuilder withKinds(List<BazelQueryKindParameters> bazelParameters) {
    String argument = BazelArgumentsUtils.getQueryKindForPatternsAndExpressions(bazelParameters);
    arguments.add(argument);

    return this;
  }

  public BazelRunnerBuilder withKindsAndExcept(
      BazelQueryKindParameters parameters, String exception) {
    String argument =
        BazelArgumentsUtils.getQueryKindForPatternsAndExpressionsWithException(
            ImmutableList.of(parameters), exception);
    arguments.add(argument);

    return this;
  }

  public BazelProcess executeBazelCommand() {
    return bazelRunner.runBazelCommand(bazelCommand, flags, arguments);
  }

  public BazelProcess executeBazelBesCommand() {
    return bazelRunner.runBazelCommandBes(bazelCommand, flags, arguments);
  }
}
