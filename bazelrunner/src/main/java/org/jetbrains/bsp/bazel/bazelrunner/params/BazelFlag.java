package org.jetbrains.bsp.bazel.bazelrunner.params;

import io.vavr.collection.List;

public class BazelFlag {
  public static String color(boolean enabled) {
    return arg("color", enabled ? "yes" : "no");
  }

  public static String outputProto() {
    return output("proto");
  }

  public static String output(String type) {
    return arg("output", type);
  }

  public static String keepGoing() {
    return flag("keep_going");
  }

  public static String outputGroups(List<String> groups) {
    return arg("output_groups", groups.mkString(","));
  }

  public static String aspect(String name) {
    return arg("aspects", name);
  }

  private static String arg(String name, String value) {
    return String.format("--%s=%s", name, value);
  }

  private static String flag(String name) {
    return "--" + name;
  }
}
