package org.jetbrains.bsp.bazel.server.bazel;

public enum BazelRunnerFlag {

  OUTPUT_PROTO ("--output=proto"),
  ASPECTS ("--aspects");

  private final String name;

  BazelRunnerFlag(String value) {
    this.name = value;
  }

  public boolean equalsName(String otherName) {
    return name.equals(otherName);
  }

  public String toString() {
    return this.name;
  }
}
