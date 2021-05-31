package org.jetbrains.bsp.bazel.server.bazel.params;

public enum BazelRunnerFlag {
  OUTPUT_PROTO("--output=proto"),
  ASPECTS("--aspects"),
  NOHOST_DEPS("--nohost_deps"),
  NOIMPLICIT_DEPS("--noimplicit_deps"),
  KEEP_GOING("--keep_going"),
  NOBUILD("--nobuild");

  private final String name;

  BazelRunnerFlag(String value) {
    name = value;
  }

  public String toString() {
    return name;
  }
}
