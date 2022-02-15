package org.jetbrains.bsp.bazel.server.bsp.utils;

import java.nio.file.Path;

public class InternalAspectsResolver {

  private final String prefix;

  public InternalAspectsResolver(Path bspProjectRoot, Path workspaceRoot) {
    var relative = workspaceRoot.relativize(bspProjectRoot).resolve(".bazelbsp").toString();
    prefix = String.format("@//%s:aspects.bzl%%", relative);
  }

  public String getAspectOutputIndicator() {
    return ".bazelbsp/aspects.bzl";
  }

  public String resolveLabel(String aspect) {
    return prefix + aspect;
  }
}
