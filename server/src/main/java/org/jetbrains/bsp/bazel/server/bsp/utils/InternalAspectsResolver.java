package org.jetbrains.bsp.bazel.server.bsp.utils;

import io.vavr.Lazy;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.commons.Constants;
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

  public String getBazelBspRoot() {
    return this.bspInfo.bazelBspDir().toString();
  }

  private String getPrefix() {
    return "@" + Constants.ASPECT_REPOSITORY + "//:aspects.bzl%";
  }
}
