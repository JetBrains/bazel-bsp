package org.jetbrains.bsp.bazel.server.bazel;

import java.util.List;
// Mnemonics are not accepted by some commands, such as the build command, therefore they must be
// treated as arguments.

public class BazelRunnerBuilderWithoutMnemonics extends BazelRunnerBuilder {
  public BazelRunnerBuilderWithoutMnemonics(BazelRunner bazelRunner, String bazelBuildCommand) {
    super(bazelRunner, bazelBuildCommand);
  }

  @Override
  public BazelRunnerBuilder withTargets(List<String> bazelTargets) {
    return withArguments(bazelTargets);
  }
}
