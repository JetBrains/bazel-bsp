package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;

public class BazelDataResolver {

  private static final String EXECUTION_ROOT_PARAMETER = "execution_root";
  private static final String WORKPLACE_ROOT_PARAMETER = "workspace";
  private static final String BAZEL_BIN_ROOT_PARAMETER = "bazel-bin";
  private static final String BAZEL_VERSION_PARAMETER = "release";

  private final BazelRunner bazelRunner;

  public BazelDataResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public BazelData resolveBazelData() {
    String execRoot = readOnlyBazelLine(EXECUTION_ROOT_PARAMETER);
    String workspaceRoot = readOnlyBazelLine(WORKPLACE_ROOT_PARAMETER);
    String binRoot = readOnlyBazelLine(BAZEL_BIN_ROOT_PARAMETER);
    String version = readOnlyBazelLine(BAZEL_VERSION_PARAMETER);
    Path workspacePath = Paths.get(execRoot);
    String workspaceLabel = workspacePath.toFile().getName();
    return new BazelData(execRoot, workspaceRoot, binRoot, workspaceLabel, version);
  }

  private String readOnlyBazelLine(String argument) {
    BazelProcessResult bazelProcessResult =
        bazelRunner
            .commandBuilder()
            .info()
            .withArgument(argument)
            .executeBazelCommand()
            .waitAndGetResult();
    List<String> output = bazelProcessResult.getStdout();

    return Iterables.getOnlyElement(output);
  }
}
