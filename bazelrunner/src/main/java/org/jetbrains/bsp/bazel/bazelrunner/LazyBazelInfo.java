package org.jetbrains.bsp.bazel.bazelrunner;

import io.vavr.Lazy;
import io.vavr.collection.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LazyBazelInfo implements BazelInfo {
  private final Lazy<Map<String, String>> bazelInfoOutput;
  private final Lazy<String> execRoot;
  private final Lazy<Path> workspaceRoot;
  private final Lazy<String> binRoot;
  private final Lazy<SemanticVersion> version;

  public LazyBazelInfo(Lazy<Map<String, String>> bazelInfoOutput) {
    this.bazelInfoOutput = bazelInfoOutput;
    this.execRoot = extract("execution_root");
    this.workspaceRoot = extract("workspace").map(Paths::get);
    this.binRoot = extract("bazel-bin");
    this.version = extract("release").map(SemanticVersion::fromReleaseData);
  }

  @Override
  public String execRoot() {
    return execRoot.get();
  }

  @Override
  public Path workspaceRoot() {
    return workspaceRoot.get();
  }

  @Override
  public String binRoot() {
    return binRoot.get();
  }

  @Override
  public SemanticVersion version() {
    return version.get();
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
