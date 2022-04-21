package org.jetbrains.bsp.bazel.bazelrunner;

import io.vavr.Lazy;
import java.nio.file.Path;

public class LazyBazelInfo implements BazelInfo {
  private final Lazy<BazelInfo> bazelInfo;

  public LazyBazelInfo(Lazy<BazelInfo> bazelInfo) {
    this.bazelInfo = bazelInfo;
  }

  @Override
  public String execRoot() {
    return bazelInfo.get().execRoot();
  }

  @Override
  public Path workspaceRoot() {
    return bazelInfo.get().workspaceRoot();
  }
}
