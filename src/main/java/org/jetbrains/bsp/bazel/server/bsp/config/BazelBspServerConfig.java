package org.jetbrains.bsp.bazel.server.bsp.config;

import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BazelBspServerConfig {

  private final String pathToBazel;
  private final List<String> targetProjectPaths;
  private final Optional<ProjectView> projectView;

  public BazelBspServerConfig(String pathToBazel, List<String> targetProjectPaths, Optional<ProjectView> projectView) {
    this.pathToBazel = pathToBazel;
    this.targetProjectPaths = targetProjectPaths;
    this.projectView = projectView;
  }

  public static BazelBspServerConfig from(String[] args, Optional<ProjectView> projectView) {
    if (args.length == 0) {
      throw new IllegalArgumentException("Configuration can't be built without any parameters");
    }

    String pathToBazel = args[0];
    List<String> targetProjectPaths =
        args.length == 2 ? Arrays.asList(args[1].split(",")) : Collections.singletonList("//...");

    return new BazelBspServerConfig(pathToBazel, targetProjectPaths, projectView);
  }

  public String getBazelPath() {
    return pathToBazel;
  }

  public List<String> getTargetProjectPaths() {
    return targetProjectPaths;
  }
}
