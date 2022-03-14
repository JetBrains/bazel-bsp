package org.jetbrains.bsp.bazel.bazelrunner;

import io.vavr.Lazy;
import io.vavr.collection.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.bazelrunner.data.SemanticVersion;

public class LazyBazelData implements BazelData {
  private final Lazy<Map<String, String>> bazelInfoOutput;
  private final Lazy<String> execRoot;
  private final Lazy<String> workspaceRoot;
  private final Lazy<String> binRoot;
  private final Lazy<SemanticVersion> version;
  private final Path bspProjectRoot;

  public LazyBazelData(Lazy<Map<String, String>> bazelInfoOutput) {
    this.bazelInfoOutput = bazelInfoOutput;
    this.execRoot = extract("execution_root");
    this.workspaceRoot = extract("workspace");
    this.binRoot = extract("bazel-bin");
    this.version = extract("release").map(SemanticVersion::fromReleaseData);
    this.bspProjectRoot = Paths.get("").toAbsolutePath().normalize();
  }

  @Override
  public String getExecRoot() {
    return execRoot.get();
  }

  @Override
  public String getWorkspaceRoot() {
    return workspaceRoot.get();
  }

  @Override
  public String getBinRoot() {
    return binRoot.get();
  }

  @Override
  public SemanticVersion getVersion() {
    return version.get();
  }

  @Override
  public Path getBspProjectRoot() {
    return bspProjectRoot;
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
