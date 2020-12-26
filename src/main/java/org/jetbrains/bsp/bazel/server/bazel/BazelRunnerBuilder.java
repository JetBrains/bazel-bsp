package org.jetbrains.bsp.bazel.server.bazel;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.data.ProcessResults;
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

  public BazelRunnerBuilder withFlags(List<String> flags) {
    this.flags.addAll(flags);

    return this;
  }

  public BazelRunnerBuilder withArgument(String argument) {
    arguments.add(argument);

    return this;
  }

  public BazelRunnerBuilder withTargets(List<String> targets) {
    String joinedTargets = BazelArgumentsUtils.getJoinedBazelTargets(targets);
    arguments.add(joinedTargets);

    return this;
  }

  public BazelRunnerBuilder withArguments(List<String> arguments) {
    this.arguments.addAll(arguments);

    return this;
  }

  public ProcessResults runBazel() {
    return bazelRunner.runBazelCommand(bazelCommand, flags, arguments);
  }

  public ProcessResults runBazelBes() {
    return bazelRunner.runBazelCommandBes(bazelCommand, flags, arguments);
  }
}
