package org.jetbrains.bsp.bazel.server.bsp.utils;

import io.vavr.Lazy;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;

public class InternalAspectsResolver {

  private final BazelInfo bazelInfo;
  private final BspInfo bspInfo;
  private final Lazy<String> prefix = Lazy.of(this::getPrefix);

  public InternalAspectsResolver(BazelInfo bazelInfo, BspInfo bspInfo) {
    this.bazelInfo = bazelInfo;
    this.bspInfo = bspInfo;
  }

  public String resolveLabel(String aspect) {
    return prefix.get() + aspect;
  }

  private String getPrefix() {
    var workspaceRoot = bazelInfo.getWorkspaceRoot();
    var bazelBspDir = bspInfo.bazelBspDir();
    var relative = workspaceRoot.relativize(bazelBspDir).toString();
    return String.format("@//%s:aspects.bzl%%", relative);
  }
}
