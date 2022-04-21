package org.jetbrains.bsp.bazel.bazelrunner;

import io.vavr.Lazy;
import io.vavr.collection.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LazyBazelInfo implements BazelInfo {
  private final Lazy<Map<String, String>> bazelInfoOutput;
  private final Lazy<String> execRoot;
  private final Lazy<Path> workspaceRoot;

  public LazyBazelInfo(Lazy<Map<String, String>> bazelInfoOutput) {
    this.bazelInfoOutput = bazelInfoOutput;
    this.execRoot = extract("execution_root");
    this.workspaceRoot = extract("workspace").map(Paths::get);
  }

  @Override
  public String execRoot() {
    return execRoot.get();
  }

  @Override
  public Path workspaceRoot() {
    return workspaceRoot.get();
  }

  private Lazy<String> extract(String name) {
    return bazelInfoOutput.map(
        map ->
            map.get(name)
                .getOrElseThrow(
                    () ->
                        new RuntimeException(
                            String.format("Failed to resolve %s from bazel info", name))));
  }
}
