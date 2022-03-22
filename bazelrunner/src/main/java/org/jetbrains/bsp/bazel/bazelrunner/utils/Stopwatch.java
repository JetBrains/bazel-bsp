package org.jetbrains.bsp.bazel.bazelrunner.utils;

import java.time.Duration;

public class Stopwatch {
  private final long start;

  private Stopwatch() {
    start = now();
  }

  public static Stopwatch start() {
    return new Stopwatch();
  }

  public Duration stop() {
    return Duration.ofMillis(now() - start);
  }

  private long now() {
    return System.currentTimeMillis();
  }
}
