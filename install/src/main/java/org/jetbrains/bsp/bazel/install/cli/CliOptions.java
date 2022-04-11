package org.jetbrains.bsp.bazel.install.cli;

import com.google.common.net.HostAndPort;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.nio.file.Path;

public class CliOptions {
  private final boolean isHelpOptionUsed;
  private final Runnable printHelp;

  private final Path workspaceRootDir;
  private final Option<Path> projectViewFilePath;

  private final Option<String> javaPath;

  private final Option<String> bazelPath;

  private final Option<HostAndPort> debuggerAddress;

  private final Option<List<String>> target;

  private final Option<List<String>> buildFlags;

  private final Option<List<String>> include;

  CliOptions(
      boolean isHelpOptionUsed,
      Runnable printHelp,
      Path workspaceRootDir,
      Option<Path> projectViewFilePath,
      Option<String> javaPath,
      Option<String> bazelPath,
      Option<HostAndPort> debuggerAddress,
      Option<List<String>> target,
      Option<List<String>> buildFlags,
      Option<List<String>> include) {
    this.isHelpOptionUsed = isHelpOptionUsed;
    this.printHelp = printHelp;
    this.workspaceRootDir = workspaceRootDir;
    this.projectViewFilePath = projectViewFilePath;
    this.javaPath = javaPath;
    this.bazelPath = bazelPath;
    this.debuggerAddress = debuggerAddress;
    this.target = target;
    this.buildFlags = buildFlags;
    this.include = include;
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

  public Option<Path> getProjectViewFilePath() {
    return projectViewFilePath;
  }

  public Option<String> getJavaPath() {
    return javaPath;
  }

  public Option<String> getBazelPath() {
    return bazelPath;
  }

  public Option<HostAndPort> getDebuggerAddress() {
    return debuggerAddress;
  }

  public Option<List<String>> getTarget() {
    return target;
  }

  public Option<List<String>> getBuildFlags() {
    return buildFlags;
  }

  public Option<List<String>> getInclude() {
    return include;
  }
}
