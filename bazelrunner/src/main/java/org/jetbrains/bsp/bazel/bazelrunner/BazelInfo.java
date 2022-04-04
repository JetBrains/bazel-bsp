package org.jetbrains.bsp.bazel.bazelrunner;

import java.nio.file.Path;

public interface BazelInfo {

  String execRoot();

  Path workspaceRoot();

  String binRoot();

  SemanticVersion version();
}
