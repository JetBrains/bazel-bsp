package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;

public class BazelDataResolver {

  private final BazelRunner bazelRunner;

  public BazelDataResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public BazelData resolveBazelData() {
    String execRoot = readOnlyBazelLine("execution_root");
    String workspaceRoot = readOnlyBazelLine("workspace");
    String binRoot = readOnlyBazelLine("bazel-bin");
    Path workspacePath = Paths.get(execRoot);
    String workspaceLabel = workspacePath.toFile().getName();
    return new BazelData(execRoot, workspaceRoot, binRoot, workspaceLabel);
  }

  private String readOnlyBazelLine(String argument) {
    BazelProcessResult bazelProcessResult =
        bazelRunner.commandBuilder().info().withArgument(argument).executeBazelCommand();
    List<String> output = bazelProcessResult.getStdout();
    return Iterables.getOnlyElement(output);
  }
}
