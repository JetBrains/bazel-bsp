package org.jetbrains.bsp.bazel.bazelrunner.utils;

import java.time.Duration;

public class Format {
  public static String duration(Duration duration) {
    return duration.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase();
  }
}
