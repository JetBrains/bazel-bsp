package org.jetbrains.bsp.bazel.bazelrunner;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
import java.util.Objects;

public class BasicBazelInfo implements BazelInfo {
  private final String execRoot;
  private final Path workspaceRoot;

  public BasicBazelInfo(
      @JsonProperty("execRoot") String execRoot,
      @JsonProperty("workspaceRoot") Path workspaceRoot) {
    this.execRoot = execRoot;
    this.workspaceRoot = workspaceRoot;
  }

  @Override
  public String execRoot() {
    return execRoot;
  }

  @Override
  public Path workspaceRoot() {
    return workspaceRoot;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BasicBazelInfo that = (BasicBazelInfo) o;
    return Objects.equals(execRoot, that.execRoot)
        && Objects.equals(workspaceRoot, that.workspaceRoot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(execRoot, workspaceRoot);
  }
}
