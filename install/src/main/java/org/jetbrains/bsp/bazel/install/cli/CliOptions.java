package org.jetbrains.bsp.bazel.install.cli;

import java.nio.file.Path;

public class CliOptions {
  private final boolean isHelpOptionUsed;
  private final Runnable printHelp;

  private final Path workspaceRootDir;
  private final Path projectViewFilePath;

  CliOptions(
      boolean isHelpOptionUsed,
      Runnable printHelp,
      Path workspaceRootDir,
      Path projectViewFilePath) {
    this.isHelpOptionUsed = isHelpOptionUsed;
    this.printHelp = printHelp;
    this.workspaceRootDir = workspaceRootDir;
    this.projectViewFilePath = projectViewFilePath;
  }

  public void printHelp() {
    printHelp.run();
  }

  public boolean isHelpOptionUsed() {
    return isHelpOptionUsed;
  }

  public Path getWorkspaceRootDir() {
    return workspaceRootDir;
  }

  public Path getProjectViewFilePath() {
    return projectViewFilePath;
  }
}
