package org.jetbrains.bsp.bazel.bazelrunner;

import java.nio.file.Path;

public interface BazelData {

  String getExecRoot();

  String getWorkspaceRoot();

  String getBinRoot();

  SemanticVersion getVersion();

  Path getBspProjectRoot();
}
