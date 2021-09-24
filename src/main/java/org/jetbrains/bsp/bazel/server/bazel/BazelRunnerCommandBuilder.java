package org.jetbrains.bsp.bazel.server.bazel;

public class BazelRunnerCommandBuilder {

  private static final String BAZEL_AQUERY_COMMAND = "aquery";
  private static final String BAZEL_BUILD_COMMAND = "build";
  private static final String BAZEL_CLEAN_COMMAND = "clean";
  private static final String BAZEL_FETCH_COMMAND = "fetch";
  private static final String BAZEL_INFO_COMMAND = "info";
  private static final String BAZEL_QUERY_COMMAND = "query";
  private static final String BAZEL_RUN_COMMAND = "run";
  private static final String BAZEL_TEST_COMMAND = "test";

  private final BazelRunner bazelRunner;

  BazelRunnerCommandBuilder(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public BazelRunnerBuilder aquery() {
    return new BazelRunnerBuilder(bazelRunner, BAZEL_AQUERY_COMMAND);
  }

  public BazelRunnerBuilder build() {
    return new BazelRunnerBuilderWithoutMnemonics(bazelRunner, BAZEL_BUILD_COMMAND);
  }

  public BazelRunnerBuilder clean() {
    return new BazelRunnerBuilder(bazelRunner, BAZEL_CLEAN_COMMAND);
  }

  public BazelRunnerBuilder fetch() {
    return new BazelRunnerBuilder(bazelRunner, BAZEL_FETCH_COMMAND);
  }

  public BazelRunnerBuilder info() {
    return new BazelRunnerBuilder(bazelRunner, BAZEL_INFO_COMMAND);
  }

  public BazelRunnerBuilder run() {
    return new BazelRunnerBuilder(bazelRunner, BAZEL_RUN_COMMAND);
  }

  public BazelRunnerBuilder query() {
    return new BazelRunnerBuilder(bazelRunner, BAZEL_QUERY_COMMAND);
  }

  public BazelRunnerBuilder test() {
    return new BazelRunnerBuilder(bazelRunner, BAZEL_TEST_COMMAND);
  }
}
