package org.jetbrains.bsp.bazel.server.bsp.info;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BspInfo {

  private final Path bspProjectRoot;

  public BspInfo(Path bspProjectRoot) {
    this.bspProjectRoot = bspProjectRoot;
  }

  public BspInfo() {
    this(Paths.get("").toAbsolutePath().normalize());
  }

  public Path bspProjectRoot() {
    return bspProjectRoot;
  }

  public Path bazelBspDir() {
    return bspProjectRoot().resolve(".bazelbsp");
  }
}
