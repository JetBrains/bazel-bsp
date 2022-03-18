package org.jetbrains.bsp.bazel.bazelrunner.outputs;

@FunctionalInterface
public interface OutputHandler {
  void onNextLine(String line);
}
