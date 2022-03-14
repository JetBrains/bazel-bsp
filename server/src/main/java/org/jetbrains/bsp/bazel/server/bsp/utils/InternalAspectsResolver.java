package org.jetbrains.bsp.bazel.server.bsp.utils;

import io.vavr.Lazy;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;

public class InternalAspectsResolver {

  private final BazelData bazelData;
  private final Lazy<String> prefix = Lazy.of(this::getPrefix);

  public InternalAspectsResolver(BazelData bazelData) {
    this.bazelData = bazelData;
  }

  public String getAspectOutputIndicator() {
    return ".bazelbsp/aspects.bzl";
  }

  public String resolveLabel(String aspect) {
    return prefix.get() + aspect;
  }

  private String getPrefix() {
    var workspaceRoot = Paths.get(bazelData.getWorkspaceRoot());
    var bspProjectRoot = bazelData.getBspProjectRoot();
    var relative = workspaceRoot.relativize(bspProjectRoot).resolve(".bazelbsp").toString();
    return String.format("@//%s:aspects.bzl%%", relative);
  }
}
