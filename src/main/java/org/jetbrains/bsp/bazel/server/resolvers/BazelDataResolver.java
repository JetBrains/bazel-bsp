package org.jetbrains.bsp.bazel.server.resolvers;

import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.bsp.bazel.server.data.BazelData;
import org.jetbrains.bsp.bazel.server.data.ProcessResults;

public class BazelDataResolver {
  private final BazelRunner bazelRunner;

  public BazelDataResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public BazelData resolveBazelData() {
    String workspaceRoot = readOnlyBazelLine("info", "workspace");
    String binRoot = readOnlyBazelLine("info", "bazel-bin");
    String execRoot = readOnlyBazelLine("info", "execution_root");
    Path workspacePath = Paths.get(execRoot);
    String workspaceLabel = workspacePath.toFile().getName();
    return new BazelData(workspaceRoot, binRoot, execRoot, workspaceLabel);
  }

  private String readOnlyBazelLine(String... args) {
    ProcessResults processResults = bazelRunner.runBazelCommand(args);
    List<String> output = processResults.getStdout();
    return Iterables.getOnlyElement(output);
  }
}
