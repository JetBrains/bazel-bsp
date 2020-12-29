package org.jetbrains.bsp.bazel.server.data;

public class BazelData {

  private final String execRoot;
  private final String workspaceRoot;
  private final String binRoot;
  private final String workspaceLabel;

  public BazelData(String execRoot, String workspaceRoot, String binRoot, String workspaceLabel) {
    this.execRoot = execRoot;
    this.workspaceRoot = workspaceRoot;
    this.binRoot = binRoot;
    this.workspaceLabel = workspaceLabel;
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
}
