package org.jetbrains.bsp.bazel.bazelrunner.outputs;

import io.vavr.collection.List;
import java.util.ArrayList;

public class OutputCollector implements OutputHandler {
  private final java.util.List<String> lines;
  private final StringBuilder stringBuilder;

  public OutputCollector() {
    lines = new ArrayList<>();
    stringBuilder = new StringBuilder();
  }

  @Override
  public void onNextLine(String line) {
    lines.add(line);
    stringBuilder.append(line);
    stringBuilder.append(System.lineSeparator());
  }

  public List<String> lines() {
    return List.ofAll(lines);
  }

  public String output() {
    return stringBuilder.toString();
  }
}
