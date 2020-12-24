package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.bsp.bazel.server.data.BazelData;

public class BazelDataResolver {

  private final BazelInfoRunner bazelInfoRunner;

  public BazelDataResolver(BazelRunner bazelRunner) {
    this.bazelInfoRunner = new BazelInfoRunner(bazelRunner);
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
    ProcessResults processResults = bazelInfoRunner.info(argument);
    List<String> output = processResults.getStdout();
    return Iterables.getOnlyElement(output);
  }
}
