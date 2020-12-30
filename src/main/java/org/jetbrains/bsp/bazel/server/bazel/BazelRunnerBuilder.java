package org.jetbrains.bsp.bazel.server.bazel;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.bazel.parameters.BazelQueryKindParameters;
import org.jetbrains.bsp.bazel.server.bazel.parameters.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bazel.utils.BazelArgumentsUtils;

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

  public BazelRunnerBuilder withFlag(BazelRunnerFlag flag) {
    flags.add(flag.toString());

    return this;
  }

  public BazelRunnerBuilder withFlag(BazelRunnerFlag flag, String value) {
    flags.add(flag.toString());
    flags.add(value);

    return this;
  }

  public BazelRunnerBuilder withArgument(String argument) {
    arguments.add(argument);

    return this;
  }

  public BazelRunnerBuilder withArguments(List<String> arguments) {
    this.arguments.addAll(arguments);

    return this;
  }

  public BazelRunnerBuilder withTargets(List<String> targets) {
    String joinedTargets = BazelArgumentsUtils.getJoinedBazelTargets(targets);
    arguments.add(joinedTargets);

    return this;
  }

  public BazelRunnerBuilder withMnemonic(List<String> targets, List<String> languageIds) {
    String argument = BazelArgumentsUtils.getMnemonicWithJoinedTargets(targets, languageIds);
    this.arguments.add(argument);

    return this;
  }

  public BazelRunnerBuilder withKind(BazelQueryKindParameters parameter) {
    return withKinds(ImmutableList.of(parameter));
  }

  public BazelRunnerBuilder withKinds(List<BazelQueryKindParameters> parameters) {
    String argument = BazelArgumentsUtils.getQueryKindForPatternsAndExpressions(parameters);
    this.arguments.add(argument);

    return this;
  }

  public BazelProcessResult executeBazelCommand() {
    return bazelRunner.runBazelCommand(bazelCommand, flags, arguments);
  }

  public BazelProcessResult executeBazelBesCommand() {
    return bazelRunner.runBazelCommandBes(bazelCommand, flags, arguments);
  }
}
