package org.jetbrains.bsp.bazel.bazelrunner.data;

import java.nio.file.Path;

public class BazelData {

  private final String execRoot;
  private final String workspaceRoot;
  private final String binRoot;
  private final String workspaceLabel;
  private final SemanticVersion version;
  private final Path bspProjectRoot;

  public BazelData(
      String execRoot,
      String workspaceRoot,
      String binRoot,
      String workspaceLabel,
      String version,
      Path bspProjectRoot) {
    this.execRoot = execRoot;
    this.workspaceRoot = workspaceRoot;
    this.binRoot = binRoot;
    this.workspaceLabel = workspaceLabel;
    this.version = SemanticVersion.fromReleaseData(version);
    this.bspProjectRoot = bspProjectRoot;
  }

  public String getExecRoot() {
    return execRoot;
  }

  public String getWorkspaceRoot() {
    return workspaceRoot;
  }

  public String getBinRoot() {
    return binRoot;
  }

  public String getWorkspaceLabel() {
    return workspaceLabel;
  }

  public SemanticVersion getVersion() {
    return version;
  }

  public Path getBspProjectRoot() {
    return bspProjectRoot;
  }
}
