package org.jetbrains.bsp.bazel.install.cli;

import java.nio.file.Path;

public class CliOptions {

  private final Path workspaceRootDir;
  private final Path projectViewFilePath;

  public CliOptions(Path workspaceRootDir, Path projectViewFilePath) {
    this.workspaceRootDir = workspaceRootDir;
    this.projectViewFilePath = projectViewFilePath;
  }

  public Path getWorkspaceRootDir() {
    return workspaceRootDir;
  }

  public Path getProjectViewFilePath() {
    return projectViewFilePath;
  }
}
